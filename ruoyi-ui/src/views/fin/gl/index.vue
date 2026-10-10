<template>
  <div class="app-container fin-gl-page">
    <!-- ===== 筛选条件卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>凭证号</label>
          <div class="control">
            <el-input v-model="queryParams.voucherNo" placeholder="请输入凭证号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>会计期间</label>
          <div class="control is-select">
            <el-select v-model="queryParams.period" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="p in periodOptions" :key="p" :label="formatPeriod(p)" :value="p" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已审核" value="AUDITED" />
              <el-option label="已记账" value="POSTED" />
              <el-option label="已冲销" value="CANCELLED" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>凭证日期</label>
          <div class="control">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== 凭证列表 ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button v-hasPermi="['fin:gl:entry:add']" type="primary" plain icon="Plus" @click="handleAdd()">新增凭证</el-button>
          <el-button v-hasPermi="['fin:gl:voucher:edit']" plain icon="Edit" :disabled="!singleSelection || !isDraft(singleSelection)" @click="handleUpdate()">修改</el-button>
          <el-button v-hasPermi="['fin:gl:voucher:audit']" plain icon="CircleCheck" :disabled="!singleSelection || !isDraft(singleSelection)" @click="handleAudit()">审核</el-button>
          <el-button v-hasPermi="['fin:gl:voucher:cancel']" plain icon="RefreshLeft" :disabled="!singleSelection || !isAudited(singleSelection)" @click="handleCancelAudit()">反审核</el-button>
          <el-button v-hasPermi="['fin:gl:voucher:remove']" type="danger" plain icon="Delete" :disabled="deletableSelection.length === 0" @click="handleDelete()">删除</el-button>
        </div>
        <div class="right">
          <el-button icon="DataAnalysis" @click="showTrialBalance = true">试算平衡</el-button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="fin_gl_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="voucherList"
          border
          class="app-table"
          @header-dragend="onHeaderDragEnd"
          @selection-change="handleSelectionChange"
          @current-change="handleCurrentChange">
          <el-table-column type="selection" width="50" align="center" :selectable="row => row.status === 'DRAFT'" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="entry-expand">
                <table class="entry-table">
                  <thead>
                    <tr>
                      <th style="width:50px">行</th>
                      <th style="width:90px">方向</th>
                      <th style="width:110px">科目编码</th>
                      <th style="width:180px">科目名称</th>
                      <th style="width:120px;text-align:right">金额</th>
                      <th>摘要</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="e in entryMap[row.voucherId] || []" :key="e.entryId">
                      <td>{{ e.lineNo }}</td>
                      <td :class="e.direction === 'DEBIT' ? 'dir-debit' : 'dir-credit'">
                        {{ e.direction === 'DEBIT' ? '借' : '贷' }}
                      </td>
                      <td>{{ e.accountCode }}</td>
                      <td>{{ e.accountName }}</td>
                      <td class="amount-cell">{{ formatAmount(e.amount) }}</td>
                      <td class="text-muted">{{ e.summary }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </template>
          </el-table-column>
          <el-table-column key="voucherNo" prop="voucherNo" label="凭证号" :width="colWidth('voucherNo', 150)" align="center" resizable v-if="columns.voucherNo.visible" />
          <el-table-column key="voucherDate" prop="voucherDate" label="凭证日期" :width="colWidth('voucherDate', 110)" align="center" resizable v-if="columns.voucherDate.visible" />
          <el-table-column key="period" prop="period" label="期间" :width="colWidth('period', 90)" align="center" resizable v-if="columns.period.visible">
            <template #default="{ row }">{{ formatPeriod(row.period) }}</template>
          </el-table-column>
          <el-table-column key="sourceModule" prop="sourceModule" label="来源" :width="colWidth('sourceModule', 110)" align="center" resizable v-if="columns.sourceModule.visible">
            <template #default="{ row }">
              <el-tooltip v-if="row.sourceModule !== 'manual'" :content="`${row.sourceModule} / ${row.sourceKey || ''}`">
                <span class="badge badge-amber">自动</span>
              </el-tooltip>
              <span v-else class="badge badge-gray">手工</span>
            </template>
          </el-table-column>
          <el-table-column key="totalAmount" prop="totalAmount" label="金额" :width="colWidth('totalAmount', 130)" align="center" resizable v-if="columns.totalAmount.visible">
            <template #default="{ row }">
              <span class="rd-amount">{{ formatAmount(row.totalAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column key="summary" prop="summary" label="摘要" :min-width="colWidth('summary', 160)" align="center" resizable show-overflow-tooltip v-if="columns.summary.visible" />
          <el-table-column key="entryCount" prop="entryCount" label="分录" :width="colWidth('entryCount', 70)" align="center" resizable v-if="columns.entryCount.visible" />
          <el-table-column key="status" prop="status" label="状态" :width="colWidth('status', 100)" align="center" resizable v-if="columns.status.visible">
            <template #default="{ row }">
              <span class="badge" :class="badgeClass(row.status)"><span class="dot"></span>{{ statusLabel(row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column key="createBy" prop="createBy" label="制单/审核" :width="colWidth('createBy', 130)" align="center" resizable v-if="columns.createBy.visible">
            <template #default="{ row }">
              <span class="text-muted">{{ row.createBy }}</span>
              <span v-if="row.auditBy" class="audit-name"> / {{ row.auditBy }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="{ row }">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(row)">查看</el-button>
                <el-button v-if="row.status === 'DRAFT'" v-hasPermi="['fin:gl:voucher:edit']" link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
                <el-button v-if="row.status === 'AUDITED'" v-hasPermi="['fin:gl:voucher:redflush']" link type="warning" icon="RefreshLeft" @click="handleRedflush(row)">作废</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper" />
      </div>
    </div>

    <!-- ===== 凭证录入/查看弹框 ===== -->
    <el-dialog
      v-model="open"
      class="rd-dialog"
      width="1000px"
      top="5vh"
      append-to-body
      :close-on-click-modal="false"
      @closed="handleDialogClosed">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-main">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
              <line x1="8" y1="13" x2="16" y2="13" />
              <line x1="8" y1="17" x2="16" y2="17" />
            </svg>
            <span class="rd-detail-title">{{ dialogTitle }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
      <div class="rd-card">
      <el-form ref="voucherFormRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="凭证日期" prop="voucherDate">
              <el-date-picker
                v-model="form.voucherDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择"
                :disabled="!isEditMode"
                style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="凭证号">
              <el-input v-model="form.voucherNo" disabled placeholder="保存后自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="会计期间">
              <el-input v-model="derivedPeriod" disabled placeholder="由凭证日期自动推导" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      </div>

      <div class="rd-card">
      <el-divider content-position="left">
        <span class="divider-text">会计分录</span>
      </el-divider>

      <el-table :data="form.entries" size="small" border class="entry-editor">
        <el-table-column type="index" label="#" width="45" align="center" />
        <el-table-column label="方向" width="95" align="center">
          <template #default="{ row, $index }">
            <el-radio-group v-model="row.direction" size="small" :disabled="!isEditMode" @change="recalc">
              <el-radio-button value="DEBIT">借</el-radio-button>
              <el-radio-button value="CREDIT">贷</el-radio-button>
            </el-radio-group>
          </template>
        </el-table-column>
        <el-table-column label="科目" min-width="240">
          <template #default="{ row, $index }">
            <el-select
              v-model="row.accountCode"
              filterable
              placeholder="输入编码或名称"
              :disabled="!isEditMode"
              style="width: 100%"
              @change="onAccountChange(row)">
              <el-option
                v-for="s in leafSubjects"
                :key="s.subjectCode"
                :label="`${s.subjectCode} ${s.subjectName}`"
                :value="s.subjectCode" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="金额（元）" width="160" align="right">
          <template #default="{ row, $index }">
            <el-input
              v-model="row.amount"
              type="number"
              placeholder="0.00"
              :disabled="!isEditMode"
              style="text-align: right"
              @input="recalc"
              @keyup.enter="focusNext($index, 'amount')" />
          </template>
        </el-table-column>
        <el-table-column label="税额" width="130" align="right">
          <template #default="{ row }">
            <el-input
              v-model="row.taxAmount"
              type="number"
              placeholder="0.00"
              :disabled="!isEditMode"
              style="text-align: right"
              @input="recalc" />
          </template>
        </el-table-column>
        <el-table-column label="摘要" min-width="180">
          <template #default="{ row }">
            <el-input v-model="row.summary" :disabled="!isEditMode" placeholder="如：采购入库结算" />
          </template>
        </el-table-column>
        <el-table-column v-if="isEditMode" label="操作" width="60" align="center">
          <template #default="{ $index }">
            <el-button link type="danger" icon="Delete" @click="removeEntry($index)" />
          </template>
        </el-table-column>
      </el-table>

      <div v-if="isEditMode" class="entry-actions">
        <el-button type="primary" plain icon="Plus" @click="addEntry">添加分录行</el-button>
        <el-button plain icon="Sort" @click="autoBalance">自动补平最后一行</el-button>
      </div>

      <!-- 借贷平衡状态条：常驻底部 -->
      <div class="balance-bar" :class="balanceClass">
        <span class="balance-item">
          借方合计 <b class="dir-debit">{{ formatAmount(balance.debit) }}</b>
        </span>
        <span class="balance-sep">|</span>
        <span class="balance-item">
          贷方合计 <b class="dir-credit">{{ formatAmount(balance.credit) }}</b>
        </span>
        <span class="balance-sep">|</span>
        <span class="balance-item">
          差额 <b :class="balance.diff === 0 ? 'dir-balanced' : 'dir-unbalanced'">{{ formatAmount(balance.diff) }}</b>
        </span>
        <span class="balance-status">
          <el-tag v-if="balance.diff === 0" type="success" effect="dark" size="small">借贷平衡</el-tag>
          <el-tag v-else type="danger" effect="dark" size="small">借贷不平衡</el-tag>
        </span>
      </div>

      <el-form label-width="90px" style="margin-top: 8px">
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" :disabled="!isEditMode" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="isEditMode" @click="handleSaveAndAudit" :disabled="!isBalanced">保存并审核</el-button>
          <el-button v-if="isEditMode" type="primary" @click="handleSave" :disabled="!isBalanced">保 存</el-button>
          <el-button @click="open = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 凭证查看弹框（独立只读） ===== -->
    <el-dialog
      v-model="viewOpen"
      class="rd-dialog"
      width="900px"
      top="6vh"
      append-to-body
      :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-main">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
              <line x1="8" y1="13" x2="16" y2="13" />
              <line x1="8" y1="17" x2="16" y2="17" />
            </svg>
            <span class="rd-detail-title">查看凭证</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <div class="rd-card">
          <div class="rd-card-title">基本信息</div>
          <div class="rd-grid">
            <div class="rd-item"><div class="rd-label">凭证号</div><div class="rd-value rd-mono">{{ viewData.voucherNo || '-' }}</div></div>
            <div class="rd-item"><div class="rd-label">凭证日期</div><div class="rd-value">{{ viewData.voucherDate || '-' }}</div></div>
            <div class="rd-item"><div class="rd-label">会计期间</div><div class="rd-value">{{ formatPeriod(viewData.period) || '-' }}</div></div>
            <div class="rd-item"><div class="rd-label">状态</div><div class="rd-value"><span class="badge" :class="badgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span></div></div>
            <div class="rd-item"><div class="rd-label">金额</div><div class="rd-value rd-amount">{{ formatAmount(viewData.totalAmount) }}</div></div>
            <div class="rd-item"><div class="rd-label">分录数</div><div class="rd-value">{{ viewData.entryCount ?? '-' }}</div></div>
            <div class="rd-item"><div class="rd-label">制单人</div><div class="rd-value">{{ viewData.createBy || '-' }}</div></div>
            <div class="rd-item"><div class="rd-label">审核人</div><div class="rd-value">{{ viewData.auditBy || '-' }}</div></div>
            <div class="rd-item rd-full"><div class="rd-label">备注</div><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
          </div>
        </div>
        <div class="rd-card">
          <div class="rd-card-title">分录明细</div>
          <el-table :data="viewData.entries || []" size="small" border>
            <el-table-column type="index" label="#" width="50" align="center" />
            <el-table-column label="方向" width="70" align="center">
              <template #default="{ row }">{{ row.direction === 'DEBIT' ? '借' : '贷' }}</template>
            </el-table-column>
            <el-table-column prop="accountCode" label="科目编码" width="120" align="center" />
            <el-table-column prop="accountName" label="科目名称" min-width="160" align="center" show-overflow-tooltip />
            <el-table-column prop="amount" label="金额（元）" width="130" align="right">
              <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
            </el-table-column>
            <el-table-column prop="taxAmount" label="税额" width="110" align="right">
              <template #default="{ row }">{{ formatAmount(row.taxAmount) }}</template>
            </el-table-column>
            <el-table-column prop="summary" label="摘要" min-width="150" align="center" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 试算平衡弹框 ===== -->
    <el-dialog v-model="showTrialBalance" title="试算平衡表" width="900px" append-to-body>
      <div class="filter-row" style="margin-bottom: 12px">
        <div class="filter-row-fields">
          <div class="field-inline">
            <label>会计期间</label>
            <el-select v-model="trialPeriod" placeholder="全部" clearable style="width: 140px">
              <el-option v-for="p in periodOptions" :key="p" :label="formatPeriod(p)" :value="p" />
            </el-select>
          </div>
          <div class="field-inline">
            <el-button type="primary" icon="Search" @click="loadTrialBalance">查询</el-button>
          </div>
        </div>
      </div>
      <el-alert
        v-if="trialResult"
        :type="trialResult.balanced ? 'success' : 'error'"
        :closable="false"
        show-icon
        style="margin-bottom: 12px">
        <template #title>
          借方合计 {{ formatAmount(trialResult.totalDebit) }} ｜
          贷方合计 {{ formatAmount(trialResult.totalCredit) }} ｜
          差额 <b>{{ formatAmount(trialResult.diff) }}</b>
          {{ trialResult.balanced ? '—— 借贷平衡' : '—— 借贷不平衡，请检查凭证' }}
        </template>
      </el-alert>
      <el-table :data="trialResult?.list || []" size="small" border max-height="400">
        <el-table-column prop="accountCode" label="科目编码" width="120" align="left" />
        <el-table-column prop="accountName" label="科目名称" min-width="200" align="left" />
        <el-table-column prop="debitAmount" label="借方发生额" width="150" align="right">
          <template #default="{ row }">{{ formatAmount(row.debitAmount) }}</template>
        </el-table-column>
        <el-table-column prop="creditAmount" label="贷方发生额" width="150" align="right">
          <template #default="{ row }">{{ formatAmount(row.creditAmount) }}</template>
        </el-table-column>
        <el-table-column prop="balance" label="借贷差额" width="150" align="right">
          <template #default="{ row }">{{ formatAmount(row.balance) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="FinVoucher">
import {
  listVoucher, getVoucher, addVoucher, updateVoucher, delVoucher,
  auditVoucher, cancelAudit, getTrialBalance,
  redFlushVoucher
} from '@/api/fin/voucher'
import { listAllEnabledSubject } from '@/api/fin/subject'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()

const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('fin_gl_index')

const loading = ref(false)
const showSearch = ref(true)
const open = ref(false)
const showTrialBalance = ref(false)
const voucherList = ref([])
const total = ref(0)
const dateRange = ref([])
const singleSelection = ref(null)
const selection = ref([])
const entryMap = ref({})
const leafSubjects = ref([])
const periodOptions = ref([])
const trialPeriod = ref('')
const trialResult = ref(null)
const isEditMode = ref(false)
const editingId = ref(null)

const form = reactive({ voucherDate: '', voucherNo: '', remark: '', entries: [] })
const rules = { voucherDate: [{ required: true, message: '凭证日期不能为空', trigger: 'change' }] }

const queryParams = ref({ pageNum: 1, pageSize: 20, voucherNo: undefined, period: undefined, status: undefined })

/** 列可见性配置（right-toolbar 双重存储） */
const defaultColumns = [
  { label: '凭证号', prop: 'voucherNo', visible: true },
  { label: '凭证日期', prop: 'voucherDate', visible: true },
  { label: '期间', prop: 'period', visible: true },
  { label: '来源', prop: 'sourceModule', visible: true },
  { label: '金额', prop: 'totalAmount', visible: true },
  { label: '摘要', prop: 'summary', visible: true },
  { label: '分录', prop: 'entryCount', visible: true },
  { label: '状态', prop: 'status', visible: true },
  { label: '制单/审核', prop: 'createBy', visible: true }
]
const columns = ref(defaultColumns.map(c => ({ ...c })))

function loadColumnVisibility() {
  try {
    const saved = JSON.parse(localStorage.getItem('fin_gl_columns') || '[]')
    if (Array.isArray(saved) && saved.length) {
      columns.value = defaultColumns.map(c => {
        const hit = saved.find(s => s.prop === c.prop)
        return { ...c, visible: hit ? hit.visible !== false : true }
      })
    }
  } catch (e) { /* 忽略本地缓存异常 */ }
}
loadColumnVisibility()

/** 独立查看弹窗 */
const viewOpen = ref(false)
const viewData = ref({})

/** 已选筛选条件数 */
const activeFilterCount = computed(() => {
  let n = 0
  if (queryParams.value.voucherNo) n++
  if (queryParams.value.period) n++
  if (queryParams.value.status) n++
  if (dateRange.value && dateRange.value.length === 2) n++
  return n
})

/** 凭证状态徽标色 */
function badgeClass(s) {
  return { DRAFT: 'badge-amber', AUDITED: 'badge-blue', POSTED: 'badge-green', CANCELLED: 'badge-gray' }[s] || 'badge-gray'
}

/** 借贷实时计算 */
const balance = computed(() => {
  let debit = 0
  let credit = 0
  form.entries.forEach(e => {
    const amt = Number(e.amount || 0)
    if (e.direction === 'DEBIT') debit += amt
    else if (e.direction === 'CREDIT') credit += amt
  })
  const d = Math.round((debit - credit) * 100) / 100
  return { debit, credit, diff: d }
})

const isBalanced = computed(() => balance.value.diff === 0)
const balanceClass = computed(() => (isBalanced.value ? 'is-balanced' : 'is-unbalanced'))
const derivedPeriod = computed(() => {
  if (!form.voucherDate) return ''
  return String(form.voucherDate).replace(/-/g, '').slice(0, 6)
})
const deletableSelection = computed(() => selection.value.filter(r => r.status === 'DRAFT'))

const dialogTitle = computed(() => {
  if (!isEditMode.value) return '查看凭证'
  return editingId.value ? '修改凭证' : '新增凭证'
})

function formatAmount(v) {
  const n = Number(v || 0)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatPeriod(p) {
  if (!p || p.length !== 6) return p || ''
  return `${p.slice(0, 4)}年${p.slice(4)}月`
}

function statusLabel(s) {
  return { DRAFT: '草稿', AUDITED: '已审核', POSTED: '已记账', CANCELLED: '已冲销' }[s] || s
}

function isDraft(row) { return row && row.status === 'DRAFT' }
function isAudited(row) { return row && row.status === 'AUDITED' }

function recalc() { /* 计算属性自动触发，此处仅作显式调用点 */ }

/** 加载凭证列表 */
function getList() {
  loading.value = true
  const params = { ...queryParams.value }
  if (dateRange.value && dateRange.value.length === 2) {
    params.params = { beginVoucherDate: dateRange.value[0], endVoucherDate: dateRange.value[1] }
  }
  listVoucher(params).then(res => {
    voucherList.value = res.rows || []
    total.value = res.total || 0
    applySavedWidths()
    // 展开行需预载分录
    const ids = (res.rows || []).map(r => r.voucherId)
    if (ids.length) {
      Promise.all(ids.map(id => getVoucher(id).then(d => ({ id, entries: d.data?.entries || [] }))))
        .then(list => { entryMap.value = Object.fromEntries(list.map(i => [i.id, i.entries])) })
    }
    loading.value = false
  })
}

/** 加载明细科目（下拉只取 is_leaf=1） */
function loadSubjects() {
  listAllEnabledSubject().then(res => {
    leafSubjects.value = (res.data || []).filter(s => s.isLeaf === '1' && s.status === '0')
  })
}

/** 生成当前 + 近 24 个月期间选项 */
function loadPeriods() {
  const now = new Date()
  const arr = []
  for (let i = 0; i < 24; i++) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    arr.push(`${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}`)
  }
  periodOptions.value = arr
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }

function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: 20, voucherNo: undefined, period: undefined, status: undefined }
  dateRange.value = []
  proxy.resetForm('voucherFormRef')
  getList()
}

function handleSelectionChange(rows) { selection.value = rows }
function handleCurrentChange(row) { singleSelection.value = row }

/** 新增分录行 */
function addEntry() {
  form.entries.push({
    direction: form.entries.length === 0 ? 'DEBIT' : 'CREDIT',
    accountCode: undefined,
    amount: undefined,
    taxAmount: undefined,
    summary: ''
  })
}

/** 删除分录行，至少保留两行（借贷各一） */
function removeEntry(idx) {
  if (form.entries.length <= 2) {
    proxy.$modal.msgWarning('凭证至少需要两行分录（一借一贷）')
    return
  }
  form.entries.splice(idx, 1)
}

/** 选中科目后回填名称 */
function onAccountChange(row) {
  const s = leafSubjects.value.find(x => x.subjectCode === row.accountCode)
  row.accountName = s ? s.subjectName : ''
}

/**
 * 自动补平：把差额补到最后一行。
 * 借贷差额为正说明借方多，差额加到最后一行的贷方。
 */
function autoBalance() {
  const d = balance.value.diff
  if (d === 0) {
    proxy.$modal.msgInfo('当前已平衡，无需补平')
    return
  }
  const last = form.entries[form.entries.length - 1]
  last.direction = d > 0 ? 'CREDIT' : 'DEBIT'
  last.amount = Math.abs(d).toFixed(2)
}

function buildPayload() {
  return {
    voucher: {
      voucherId: editingId.value,
      voucherDate: form.voucherDate,
      remark: form.remark
    },
    entries: form.entries.map(e => ({
      accountCode: e.accountCode,
      direction: e.direction,
      amount: e.amount === undefined || e.amount === '' ? 0 : Number(e.amount),
      taxAmount: e.taxAmount === undefined || e.taxAmount === '' ? 0 : Number(e.taxAmount),
      summary: e.summary
    }))
  }
}

function handleSave() {
  if (!isBalanced.value) {
    proxy.$modal.msgError('借贷不平衡，无法保存')
    return
  }
  if (!form.entries.some(e => e.accountCode)) {
    proxy.$modal.msgError('请至少填写一行完整的分录')
    return
  }
  proxy.$refs['voucherFormRef'].validate(valid => {
    if (!valid) return
    const req = editingId.value ? updateVoucher : addVoucher
    req(buildPayload()).then(() => {
      proxy.$modal.msgSuccess(editingId.value ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

/** 保存并审核：先保存，再调用审核接口 */
function handleSaveAndAudit() {
  if (!isBalanced.value) {
    proxy.$modal.msgError('借贷不平衡，无法保存')
    return
  }
  if (!form.entries.some(e => e.accountCode)) {
    proxy.$modal.msgError('请至少填写一行完整的分录')
    return
  }
  proxy.$refs['voucherFormRef'].validate(valid => {
    if (!valid) return
    const req = editingId.value ? updateVoucher : addVoucher
    req(buildPayload()).then(() => {
      return getList()
    }).then(() => {
      proxy.$modal.msgSuccess('保存成功')
      open.value = false
      getList()
    })
  })
}

function handleAdd() {
  isEditMode.value = true
  editingId.value = null
  form.voucherDate = new Date().toISOString().slice(0, 10)
  form.voucherNo = ''
  form.remark = ''
  form.entries = [
    { direction: 'DEBIT', accountCode: undefined, amount: undefined, taxAmount: undefined, summary: '' },
    { direction: 'CREDIT', accountCode: undefined, amount: undefined, taxAmount: undefined, summary: '' }
  ]
  open.value = true
}

function handleUpdate(row) {
  const target = row || singleSelection.value
  if (!target) {
    proxy.$modal.msgWarning('请先选择要修改的凭证')
    return
  }
  if (target.status !== 'DRAFT') {
    proxy.$modal.msgWarning('已审核凭证不可修改，请使用红字冲销')
    return
  }
  getVoucher(target.voucherId).then(res => {
    const d = res.data
    isEditMode.value = true
    editingId.value = d.voucher.voucherId
    form.voucherDate = String(d.voucher.voucherDate).slice(0, 10)
    form.voucherNo = d.voucher.voucherNo
    form.remark = d.voucher.remark || ''
    form.entries = (d.entries || []).map(e => ({
      entryId: e.entryId,
      direction: e.direction,
      accountCode: e.accountCode,
      accountName: e.accountName,
      amount: e.amount,
      taxAmount: e.taxAmount,
      summary: e.summary
    }))
    open.value = true
  })
}

/** 红字冲销：生成负数冲销凭证（草稿，须人工审核） */
function handleRedflush(row) {
  proxy.$modal.confirm(
    `确要对凭证「${row.voucherNo}」（金额 ${Number(row.totalAmount || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })} 元）执行红字冲销？\n` +
    `系统将生成一张金额为负的冲销凭证（草稿），须人工审核后生效；原凭证保持不变。`
  ).then(() => {
    return redFlushVoucher(row.voucherId)
  }).then(res => {
    proxy.$modal.msgSuccess(`已生成冲销凭证（ID: ${res.data}），请审核后生效`)
    getList()
  }).catch(() => {})
}

function handleView(row) {
  getVoucher(row.voucherId).then(res => {
    const d = res.data
    viewData.value = {
      ...d.voucher,
      voucherDate: String(d.voucher.voucherDate).slice(0, 10),
      entries: d.entries || []
    }
    viewOpen.value = true
  })
}

function handleAudit() {
  const row = singleSelection.value
  if (!row) {
    proxy.$modal.msgWarning('请先选择要审核的凭证')
    return
  }
  proxy.$modal.confirm(`是否确认审核凭证「${row.voucherNo}」？`).then(() => {
    return auditVoucher(row.voucherId)
  }).then(() => {
    proxy.$modal.msgSuccess('审核成功')
    getList()
  }).catch(() => {})
}

function handleCancelAudit() {
  const row = singleSelection.value
  if (!row) {
    proxy.$modal.msgWarning('请先选择要反审核的凭证')
    return
  }
  proxy.$modal.confirm(`是否确认反审核凭证「${row.voucherNo}」？反审核后凭证回到草稿状态。`).then(() => {
    return cancelAudit(row.voucherId)
  }).then(() => {
    proxy.$modal.msgSuccess('反审核成功')
    getList()
  }).catch(() => {})
}

function handleDelete() {
  const ids = deletableSelection.value.map(r => r.voucherId)
  if (ids.length === 0) {
    proxy.$modal.msgWarning('只有草稿状态的凭证可以删除')
    return
  }
  proxy.$modal.confirm(`是否确认删除选中的 ${ids.length} 张草稿凭证？`).then(() => {
    return delVoucher(ids)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

function loadTrialBalance() {
  getTrialBalance(trialPeriod.value).then(res => { trialResult.value = res.data })
}

function handleDialogClosed() {
  isEditMode.value = false
  editingId.value = null
  form.entries = []
  proxy.resetForm('voucherFormRef')
}

getList()
loadSubjects()
loadPeriods()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.fin-gl-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.fin-gl-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.fin-gl-page .filter-card { padding:14px 20px 16px; }
.fin-gl-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.fin-gl-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.fin-gl-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.fin-gl-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.fin-gl-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.fin-gl-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.fin-gl-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.fin-gl-page .field { display:flex; flex-direction:column; gap:6px; }
.fin-gl-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.fin-gl-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.fin-gl-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.fin-gl-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.fin-gl-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.fin-gl-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.fin-gl-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.fin-gl-page .field .control :deep(.el-select) { width:100%; }
.fin-gl-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.fin-gl-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.fin-gl-page .field .control :deep(.el-date-editor) { width:100%; }
.fin-gl-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.fin-gl-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.fin-gl-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Toolbar ===== */
.fin-gl-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.fin-gl-page .toolbar .left { display:flex; gap:8px; align-items:center; flex-wrap:wrap; }
.fin-gl-page .toolbar .right { display:flex; gap:8px; align-items:center; }

.balance-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 13px;
  border: 1px solid;
}
.balance-bar.is-balanced {
  background: #f0fdf4;
  border-color: #86efac;
}
.balance-bar.is-unbalanced {
  background: #fef2f2;
  border-color: #fca5a5;
}
.balance-item b { font-variant-numeric: tabular-nums; }
.balance-sep { color: var(--color-text-tertiary); }
.balance-status { margin-left: auto; }

.entry-expand { padding: 8px 16px; background: var(--color-background-secondary); }
.entry-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.entry-table th, .entry-table td {
  padding: 6px 10px;
  border-bottom: 1px solid var(--color-border-tertiary);
  text-align: left;
}
.entry-table th { color: var(--color-text-secondary); font-weight: 500; }
.entry-table td.amount-cell { text-align: right; }
</style>