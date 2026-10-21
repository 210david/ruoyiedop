<template>
  <div class="app-container fin-list-page fin-ap-page">
    <!-- 应付总览（stat-card 样式，对齐应收管理页面） -->
    <el-row :gutter="12" class="mb8">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">应付总额（元）</div>
          <div class="stat-value">{{ formatAmount(summary.totalAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-success">
          <div class="stat-label">已付（元）</div>
          <div class="stat-value">{{ formatAmount(summary.paidAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-warning">
          <div class="stat-label">未付（元）</div>
          <div class="stat-value">{{ formatAmount(summary.unpaidAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-danger">
          <div class="stat-label">逾期未付</div>
          <div class="stat-value">{{ overdueList.length }}<span class="stat-unit">笔</span></div>
        </div>
      </el-col>
    </el-row>

    <!-- ===== 筛选卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <div class="adv-link" :class="{ 'is-open': showAdvanced }" @click="showAdvanced = !showAdvanced">
          高级筛选<el-icon class="chev"><ArrowDown /></el-icon>
        </div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>应付单号</label>
          <div class="control">
            <el-input v-model="queryParams.apNo" placeholder="请输入单号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.partnerName" placeholder="请输入名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option label="未核销" value="OPEN" />
              <el-option label="部分核销" value="PARTIAL" />
              <el-option label="已核销" value="SETTLED" />
              <el-option label="已作废" value="VOID" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>应付日期</label>
          <div class="control">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>会计期间</label>
          <div class="control is-select">
            <el-select v-model="queryParams.period" placeholder="全部" clearable filterable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="p in periodOptions" :key="p" :label="formatPeriod(p)" :value="p" />
            </el-select>
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
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button v-hasPermi="['fin:ap:add']" type="primary" icon="Plus" @click="handleGenerate">从发票生成应付单</el-button>
          <el-button v-hasPermi="['fin:ap:pay']" type="warning" icon="Refresh" @click="handleSyncPaid">同步已付金额</el-button>
          <el-button icon="DataAnalysis" @click="showAging = true">账龄分析</el-button>
          <el-button icon="Warning" @click="showOverdue = true">逾期预警</el-button>
          <el-button v-hasPermi="['fin:ap:export']" icon="Download" @click="handleExport">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="fin_ap_columns" />
          <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
        </div>
      </div>

      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>
          <span class="hint-text">
            应付单由<b>已审核的采购发票</b>自动生成，金额不可手工修改；
            已付金额以采购系统付款记录为准，财务侧不重复登记，避免重复付款。
          </span>
        </template>
      </el-alert>

      <div class="table-wrap">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="apList"
          border
          class="app-table"
          highlight-current
          @current-change="handleCurrentChange"
          @header-dragend="onHeaderDragEnd">
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column prop="apNo" label="应付单号" key="apNo" v-if="columns.apNo.visible" :width="colWidth('应付单号', 160)" resizable align="center">
            <template #default="{ row }"><span class="col-mono">{{ row.apNo }}</span></template>
          </el-table-column>
          <el-table-column prop="partnerName" label="供应商" key="partnerName" v-if="columns.partnerName.visible" :min-width="colWidth('供应商', 200)" resizable align="center" show-overflow-tooltip />
          <el-table-column prop="sourceKey" label="来源发票" key="sourceKey" v-if="columns.sourceKey.visible" :width="colWidth('来源发票', 150)" resizable align="center">
            <template #default="{ row }">
              <el-tooltip :content="`来源：${row.sourceModule} / ${row.sourceType}`">
                <span class="text-muted">{{ row.sourceKey || '手工' }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="apDate" label="应付日期" key="apDate" v-if="columns.apDate.visible" :width="colWidth('应付日期', 120)" resizable align="center" />
          <el-table-column prop="amountExclTax" label="不含税" key="amountExclTax" v-if="columns.amountExclTax.visible" :width="colWidth('不含税', 120)" resizable align="center">
            <template #default="{ row }">{{ formatAmount(row.amountExclTax) }}</template>
          </el-table-column>
          <el-table-column prop="taxAmount" label="税额" key="taxAmount" v-if="columns.taxAmount.visible" :width="colWidth('税额', 110)" resizable align="center">
            <template #default="{ row }">{{ formatAmount(row.taxAmount) }}</template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="价税合计" key="totalAmount" v-if="columns.totalAmount.visible" :width="colWidth('价税合计', 130)" resizable align="center">
            <template #default="{ row }">
              <span class="rd-amount">{{ formatAmount(row.totalAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="paidAmount" label="已付" key="paidAmount" v-if="columns.paidAmount.visible" :width="colWidth('已付', 120)" resizable align="center">
            <template #default="{ row }">{{ formatAmount(row.paidAmount) }}</template>
          </el-table-column>
          <el-table-column prop="unpaidAmount" label="未付" key="unpaidAmount" v-if="columns.unpaidAmount.visible" :width="colWidth('未付', 130)" resizable align="center">
            <template #default="{ row }">
              <span class="rd-amount text-unpaid">
                {{ formatAmount(Number(row.totalAmount || 0) - Number(row.paidAmount || 0)) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="dueDate" label="到期日" key="dueDate" v-if="columns.dueDate.visible" :width="colWidth('到期日', 120)" resizable align="center">
            <template #default="{ row }">
              <span :class="isOverdue(row) ? 'text-danger' : 'text-muted'">
                {{ row.dueDate ? String(row.dueDate).slice(0, 10) : '—' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" key="status" v-if="columns.status.visible" :width="colWidth('状态', 100)" resizable align="center">
            <template #default="{ row }">
              <span class="badge" :class="badgeClass(row.status)">
                <span class="dot"></span>{{ statusLabel(row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right" class-name="col-action">
            <template #default="{ row }">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(row)">详情</el-button>
                <el-button v-if="canRowWriteoff(row)" v-hasPermi="['fin:ap:writeoff']"
                           link type="success" icon="CircleCheck" @click="handleRowWriteoff(row)">核销</el-button>
                <el-button v-if="row.status !== 'SETTLED' && row.status !== 'VOID'"
                           v-hasPermi="['fin:ap:edit']" link type="warning" icon="Close" @click="handleVoid(row)">作废</el-button>
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

    <!-- ===== 应付单详情（只读弹窗） ===== -->
    <el-dialog v-model="viewOpen" title="" width="860px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon><Document /></el-icon></div>
          <div>
            <div class="rd-detail-header-title">应付单详情</div>
            <div class="rd-detail-header-sub">
              单号 <span class="rd-detail-header-no">{{ current?.apNo || '—' }}</span>
              <span class="badge" :class="badgeClass(current?.status)" style="margin-left: 8px">
                <span class="dot"></span>{{ statusLabel(current?.status) }}
              </span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page" v-if="current">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><el-icon><InfoFilled /></el-icon></span> 基本信息</div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item"><div class="rd-label">应付单号</div><div class="rd-value">{{ current.apNo || '暂无' }}</div></div>
              <div class="rd-item"><div class="rd-label">供应商</div><div class="rd-value">{{ current.partnerName || '暂无' }}</div></div>
              <div class="rd-item"><div class="rd-label">来源发票</div><div class="rd-value">{{ current.sourceKey || '手工' }}</div></div>
              <div class="rd-item"><div class="rd-label">发票号码</div><div class="rd-value">{{ current.invoiceNo || '暂无' }}</div></div>
              <div class="rd-item"><div class="rd-label">应付日期</div><div class="rd-value">{{ current.apDate || '暂无' }}</div></div>
              <div class="rd-item"><div class="rd-label">会计期间</div><div class="rd-value">{{ formatPeriod(current.period) || '暂无' }}</div></div>
              <div class="rd-item"><div class="rd-label">不含税金额</div><div class="rd-value">{{ formatAmount(current.amountExclTax) }}</div></div>
              <div class="rd-item"><div class="rd-label">税额</div><div class="rd-value">{{ formatAmount(current.taxAmount) }}</div></div>
              <div class="rd-item"><div class="rd-label">价税合计</div><div class="rd-value rd-value--large"><span class="rd-amount">{{ formatAmount(current.totalAmount) }}</span></div></div>
              <div class="rd-item"><div class="rd-label">已付金额</div><div class="rd-value">{{ formatAmount(current.paidAmount) }}</div></div>
              <div class="rd-item"><div class="rd-label">未付余额</div><div class="rd-value rd-value--large"><span class="rd-amount">{{ formatAmount(detailUnpaid) }}</span></div></div>
              <div class="rd-item"><div class="rd-label">账期</div><div class="rd-value">{{ current.paymentDays ? current.paymentDays + ' 天' : '暂无' }}</div></div>
              <div class="rd-item"><div class="rd-label">到期日</div><div class="rd-value">{{ current.dueDate || '暂无' }}</div></div>
              <div class="rd-item"><div class="rd-label">状态</div><div class="rd-value"><span class="badge" :class="badgeClass(current.status)"><span class="dot"></span>{{ statusLabel(current.status) }}</span></div></div>
              <div class="rd-item rd-item--full"><div class="rd-label">备注</div><div class="rd-value">{{ current.remark || '暂无' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><el-icon><Tickets /></el-icon></span> 核销记录</div>
          </div>
          <div class="rd-card-body">
            <el-table :data="writeoffList" border>
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column prop="writeoffDate" label="核销日期" align="center" />
              <el-table-column prop="writeoffAmount" label="核销金额" align="center">
                <template #default="{ row }"><span class="rd-amount">{{ formatAmount(row.writeoffAmount) }}</span></template>
              </el-table-column>
              <el-table-column prop="createBy" label="核销人" align="center" />
              <el-table-column prop="createTime" label="核销时间" align="center" />
              <el-table-column prop="remark" label="备注" align="center" show-overflow-tooltip />
              <template #empty>
                <span class="text-muted">尚未核销</span>
              </template>
            </el-table>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 核销弹框 -->
    <el-dialog v-model="showWriteoff" width="480px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="20"><CircleCheck /></el-icon></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">付款核销</div>
            <div class="rd-detail-header-sub">
              <span class="rd-detail-header-no">{{ current?.apNo || '' }}</span>
            </div>
          </div>
        </div>
      </template>
      <el-form label-width="90px">
        <el-form-item label="应付单">
          <el-input :model-value="current?.apNo" disabled />
        </el-form-item>
        <el-form-item label="未付余额">
          <el-input :model-value="formatAmount(detailUnpaid)" disabled />
        </el-form-item>
        <el-form-item label="核销金额" required>
          <el-input v-model="writeoffAmount" type="number" :max="detailUnpaid" placeholder="请输入核销金额" />
        </el-form-item>
        <el-form-item label="快速填写">
          <el-button size="small" @click="writeoffAmount = detailUnpaid">全额 {{ formatAmount(detailUnpaid) }}</el-button>
          <el-button size="small" @click="writeoffAmount = (Number(writeoffAmount) || 0).toFixed(2)">对齐两位小数</el-button>
        </el-form-item>
        <el-form-item label="核销备注">
          <el-input v-model="writeoffRemark" type="textarea" :rows="2" placeholder="选填，将留痕" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitWriteoff">确 定</el-button>
          <el-button @click="showWriteoff = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 生成结果 -->
    <el-dialog v-model="showResult" width="440px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="20"><Tickets /></el-icon></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">应付单生成结果</div>
            <div class="rd-detail-header-sub">
              <span class="rd-detail-header-no">生成执行留痕</span>
            </div>
          </div>
        </div>
      </template>
      <el-descriptions :column="1" border v-if="lastResult">
        <el-descriptions-item label="待处理发票">{{ lastResult.total }}</el-descriptions-item>
        <el-descriptions-item label="成功">
          <span class="text-success">{{ lastResult.success }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="跳过">{{ lastResult.skip }}</el-descriptions-item>
        <el-descriptions-item label="失败">
          <span class="text-danger">{{ lastResult.fail }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="showResult = false">知道了</el-button>
      </template>
    </el-dialog>

    <!-- 账龄分析 -->
    <el-dialog v-model="showAging" width="720px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="20"><DataAnalysis /></el-icon></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">应付账龄分析</div>
            <div class="rd-detail-header-sub">
              <span class="rd-detail-header-no">按账龄区间统计未付款项</span>
            </div>
          </div>
        </div>
      </template>
      <div style="margin-bottom: 12px">
        <el-button type="primary" icon="Search" @click="loadAging">查询</el-button>
      </div>
      <el-table :data="agingList" size="small" border>
        <el-table-column prop="bucket" label="账龄区间" width="180" align="center" />
        <el-table-column prop="cnt" label="笔数" width="100" align="right" />
        <el-table-column label="应付总额" width="150" align="right">
          <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="未付金额" width="150" align="right">
          <template #default="{ row }">
            <span class="text-unpaid">{{ formatAmount(row.unpaidAmount) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 逾期预警 -->
    <el-dialog v-model="showOverdue" width="880px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="20"><Warning /></el-icon></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">逾期未付预警</div>
            <div class="rd-detail-header-sub">
              <span class="rd-detail-header-no">已到期仍未付清的应付单</span>
            </div>
          </div>
        </div>
      </template>
      <el-table :data="overdueList" size="small" border max-height="420">
        <el-table-column prop="apNo" label="应付单号" width="150" align="left" />
        <el-table-column prop="partnerName" label="供应商" min-width="180" align="left" />
        <el-table-column prop="dueDate" label="到期日" width="110" align="center" />
        <el-table-column label="未付金额" width="140" align="right">
          <template #default="{ row }">
            <span class="text-danger">{{ formatAmount(Number(row.totalAmount || 0) - Number(row.paidAmount || 0)) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="账龄" width="100" align="center">
          <template #default="{ row }">{{ overdueDays(row) }} 天</template>
        </el-table-column>
      </el-table>
    </el-dialog>
    <el-dialog v-model="showStatusHelp" title="应付管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content"><h4>一、状态流转图</h4><div class="status-flow"><el-tag type="info">待付</el-tag><span class="flow-arrow">→</span><el-tag type="primary">部分付款</el-tag><span class="flow-arrow">→</span><el-tag type="success">已付清</el-tag><span class="flow-arrow">→</span><el-tag type="warning">已核销</el-tag></div><h4>二、各状态说明</h4><el-descriptions :column="1" border><el-descriptions-item label="待付">应付单已生成，尚未收到任何付款，可跟踪付款进度</el-descriptions-item><el-descriptions-item label="部分付款">已收到部分款项，剩余应付金额继续跟踪</el-descriptions-item><el-descriptions-item label="已付清">应付金额全部付清，单据付款完成</el-descriptions-item><el-descriptions-item label="已核销">应付单与付款完成核销匹配，往来账务闭环</el-descriptions-item></el-descriptions><h4>三、重点业务规则</h4><el-row :gutter="16"><el-col :span="12"><div class="highlight-card highlight-primary"><div class="highlight-card-title">发票自动生成应付单</div><div class="highlight-card-body">从采购发票生成应付单，系统自动<strong>编号（YF 前缀）</strong>并关联来源单据，无需手工录入</div></div></el-col><el-col :span="12"><div class="highlight-card highlight-warning"><div class="highlight-card-title">同步已付金额</div><div class="highlight-card-body">付款完成后点击「同步已付金额」刷新实付数据；<strong>未同步前</strong>单据状态不会变化</div></div></el-col></el-row><el-row :gutter="16" style="margin-top: 12px;"><el-col :span="12"><div class="highlight-card highlight-success"><div class="highlight-card-title">核销闭环</div><div class="highlight-card-body">应付核销将应付单与付款冲抵匹配，<strong>核销金额不得超过单据金额</strong>，保证往来账务闭环</div></div></el-col><el-col :span="12"><div class="highlight-card highlight-danger"><div class="highlight-card-title">账龄与逾期预警</div><div class="highlight-card-body">账龄分析与逾期预警持续监控超账期未付清的应付单据，<strong>超期单据会被标记提示</strong></div></div></el-col></el-row><h4>四、业务操作流程</h4><el-timeline><el-timeline-item type="primary" :hollow="true"><strong>生成应付单：</strong>点击「从发票生成应付单」，选择采购发票自动生成，核对金额与供应商</el-timeline-item><el-timeline-item type="warning" :hollow="true"><strong>付款跟踪：</strong>供应商付款后点击「同步已付金额」，更新实付金额与单据状态</el-timeline-item><el-timeline-item type="success" :hollow="true"><strong>应付核销：</strong>单据付清后执行核销，冲抵往来欠款</el-timeline-item><el-timeline-item type="info" :hollow="true"><strong>账龄监控：</strong>通过账龄分析与逾期预警持续跟踪超期应付款</el-timeline-item></el-timeline></div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="FinAp">
const showStatusHelp = ref(false)
import {
  listAp, getAp, generateAp, syncPaid, writeoffAp, voidAp, getApAging, getApOverdue, getApSummary
} from '@/api/fin/ap'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('fin_ap_index')

// 列显隐配置（localStorage 持久化，key: fin_ap_columns）
const defaultColumns = {
  apNo: { label: '应付单号', visible: true },
  partnerName: { label: '供应商', visible: true },
  sourceKey: { label: '来源发票', visible: true },
  apDate: { label: '应付日期', visible: true },
  amountExclTax: { label: '不含税', visible: true },
  taxAmount: { label: '税额', visible: true },
  totalAmount: { label: '价税合计', visible: true },
  paidAmount: { label: '已付', visible: true },
  unpaidAmount: { label: '未付', visible: true },
  dueDate: { label: '到期日', visible: true },
  status: { label: '状态', visible: true }
}

// 从 localStorage 读取保存的列显隐配置
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('fin_ap_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}

const columns = ref(loadColumnVisibility())

const loading = ref(false)
const showSearch = ref(true)
const apList = ref([])
const total = ref(0)
const dateRange = ref([])
const current = ref(null)
const writeoffList = ref([])
const summary = ref({})
const agingList = ref([])
const overdueList = ref([])
const lastResult = ref(null)

const viewOpen = ref(false)
const showWriteoff = ref(false)
const showResult = ref(false)
const showAging = ref(false)
const showOverdue = ref(false)
const writeoffAmount = ref('')
const writeoffRemark = ref('')

// 已选筛选条件数
const activeFilterCount = computed(() => {
  let n = 0
  if (queryParams.value.apNo) n++
  if (queryParams.value.partnerName) n++
  if (queryParams.value.status) n++
  if (dateRange.value && dateRange.value.length === 2) n++
  if (queryParams.value.period) n++
  return n
})

const showAdvanced = ref(false)

// 会计期间选项：近 24 个自然月（2026-10 倒推）
const periodOptions = computed(() => {
  const list = []
  const now = new Date()
  for (let i = 0; i < 24; i++) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    list.push(`${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}`)
  }
  return list
})

const queryParams = ref({ pageNum: 1, pageSize: 20, apNo: undefined, partnerName: undefined, status: undefined, period: undefined })

const detailUnpaid = computed(() => {
  if (!current.value) return 0
  return Number(current.value.totalAmount || 0) - Number(current.value.paidAmount || 0)
})

function formatAmount(v) {
  return Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatPeriod(p) {
  if (!p || p.length !== 6) return p || ''
  return `${p.slice(0, 4)}年${p.slice(4)}月`
}

function statusLabel(s) {
  return { OPEN: '未核销', PARTIAL: '部分核销', SETTLED: '已核销', VOID: '已作废' }[s] || s
}

function badgeClass(s) {
  return { OPEN: 'badge-amber', PARTIAL: 'badge-blue', SETTLED: 'badge-green', VOID: 'badge-gray' }[s] || 'badge-gray'
}

function isOverdue(row) {
  if (!row.dueDate || ['SETTLED', 'VOID'].includes(row.status)) return false
  if (Number(row.totalAmount || 0) - Number(row.paidAmount || 0) <= 0) return false
  return new Date(row.dueDate) < new Date()
}

function overdueDays(row) {
  if (!row.dueDate) return 0
  return Math.floor((Date.now() - new Date(row.dueDate).getTime()) / 86400000)
}

function getList() {
  loading.value = true
  const params = { ...queryParams.value }
  delete params.apNo
  delete params.period
  if (dateRange.value && dateRange.value.length === 2) {
    params.params = { beginApDate: dateRange.value[0], endApDate: dateRange.value[1] }
  }
  listAp(params).then(res => {
    let rows = res.rows || []
    // apNo / 会计期间后端列表接口不支持，前端过滤（同 fin/config 做法）
    if (queryParams.value.apNo) {
      const kw = String(queryParams.value.apNo).trim().toLowerCase()
      rows = rows.filter(r => String(r.apNo || '').toLowerCase().includes(kw))
    }
    if (queryParams.value.period) {
      rows = rows.filter(r => String(r.period || '') === String(queryParams.value.period))
    }
    if (queryParams.value.apNo || queryParams.value.period) {
      total.value = rows.length
    } else {
      total.value = res.total || 0
    }
    apList.value = rows
    loading.value = false
    applySavedWidths()
  })
}

function loadSummary() {
  getApSummary().then(res => { summary.value = res.data || {} })
}

function loadOverdue() {
  getApOverdue().then(res => { overdueList.value = res.data || [] })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }

function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: 20, apNo: undefined, partnerName: undefined, status: undefined, period: undefined }
  dateRange.value = []
  getList()
}

function handleCurrentChange(row) { current.value = row }

/** 从发票生成应付单 */
function handleGenerate() {
  proxy.$modal.confirm('将扫描已审核的采购发票生成应付单，是否继续？').then(() => {
    return generateAp()
  }).then(res => {
    lastResult.value = res.data
    showResult.value = true
    getList(); loadSummary(); loadOverdue()
  }).catch(() => {})
}

/** 同步已付金额 */
function handleSyncPaid() {
  proxy.$modal.confirm('将从采购系统同步最新的已付金额，是否继续？').then(() => {
    return syncPaid()
  }).then(res => {
    proxy.$modal.msgSuccess(`已同步 ${res.data} 张应付单的已付金额`)
    getList(); loadSummary(); loadOverdue()
  }).catch(() => {})
}

function handleView(row) {
  getAp(row.apId).then(res => {
    current.value = res.data.ap
    writeoffList.value = res.data.writeoffs || []
    viewOpen.value = true
  })
}

/** 行级核销：未核销/部分核销且有未付余额的应付单可核销 */
function canRowWriteoff(row) {
  if (!row || ['SETTLED', 'VOID'].includes(row.status)) return false
  return Number(row.totalAmount || 0) - Number(row.paidAmount || 0) > 0
}

function handleRowWriteoff(row) {
  current.value = row
  writeoffAmount.value = (Number(row.totalAmount || 0) - Number(row.paidAmount || 0)).toFixed(2)
  writeoffRemark.value = ''
  showWriteoff.value = true
}

function submitWriteoff() {
  const amt = Number(writeoffAmount.value)
  if (!amt || amt <= 0) {
    proxy.$modal.msgError('核销金额必须大于 0')
    return
  }
  if (amt > detailUnpaid.value) {
    proxy.$modal.msgError(`核销金额不能超过未付余额 ${formatAmount(detailUnpaid.value)}`)
    return
  }
  writeoffAp(current.value.apId, amt, writeoffRemark.value).then(() => {
    proxy.$modal.msgSuccess('核销成功')
    showWriteoff.value = false
    getList(); loadSummary(); loadOverdue()
    // 刷新当前单据，避免详情弹框显示过期数据
    if (current.value && current.value.apId) {
      getAp(current.value.apId).then(res => {
        current.value = res.data.ap
        writeoffList.value = res.data.writeoffs || []
      })
    }
  })
}

function handleVoid(row) {
  proxy.$modal.confirm(`是否确认作废应付单「${row.apNo}」？作废后不再参与账龄统计。`).then(() => {
    return voidAp(row.apId, '手工作废')
  }).then(() => {
    proxy.$modal.msgSuccess('作废成功')
    getList(); loadSummary(); loadOverdue()
  }).catch(() => {})
}

function loadAging() {
  getApAging().then(res => { agingList.value = res.data || [] })
}

function handleExport() {
  proxy.download('/fin/ap/export', { ...queryParams.value }, `应付单_${new Date().getTime()}.xlsx`)
}

getList()
loadSummary()
loadOverdue()
</script>

<style scoped>
.hint-text { font-size: 12px; }
.divider-text { font-size: 13px; font-weight: 500; color: var(--color-text-secondary); }

/* ===== stat-card（对齐应收管理页面） ===== */
.fin-ap-page .stat-card {
  background: #fff;
  border: 1px solid var(--ink-200, #e5e7eb);
  border-radius: var(--r-lg, 14px);
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  padding: 16px 18px;
  text-align: center;
  transition: all .15s var(--ease-out, ease);
}
.fin-ap-page .stat-card:hover {
  border-color: var(--brand-200, #c7d2fe);
  box-shadow: 0 4px 12px -2px rgba(99, 102, 241, .12);
}
.fin-ap-page .stat-card .stat-label { font-size: 13px; color: var(--ink-500, #6b7280); margin-bottom: 8px; }
.fin-ap-page .stat-card .stat-value { font-size: 22px; font-weight: 700; color: var(--ink-900, #0f172a); font-feature-settings: "tnum" 1; font-variant-numeric: tabular-nums; }
.fin-ap-page .stat-card.stat-success .stat-value { color: var(--green-500, #10b981); }
.fin-ap-page .stat-card.stat-warning .stat-value { color: var(--amber-500, #f59e0b); }
.fin-ap-page .stat-card.stat-danger .stat-value { color: var(--red-500, #ef4444); }
.fin-ap-page .stat-unit { font-size: 12px; margin-left: 2px; font-weight: 400; }

.amount-cell { font-variant-numeric: tabular-nums; }
.amount-cell.strong { font-weight: 600; }
.text-muted { color: var(--color-text-tertiary); }
.text-paid { color: #16a34a; }
.text-unpaid { color: #b85450; }
.text-success { color: #16a34a; font-weight: 600; }
.text-danger { color: #dc2626; font-weight: 600; }

/* ===== 筛选卡片（对齐 mk 列表页规范） ===== */
.fin-ap-page .filter-card .filter-head { display: flex; align-items: center; justify-content: space-between; }
.fin-ap-page .adv-link { display: inline-flex; align-items: center; gap: 4px; border: none; background: none; cursor: pointer; font-size: 13px; font-weight: 500; color: var(--brand-600, #4f46e5); padding: 4px 8px; border-radius: 6px; transition: background .2s var(--ease-out, ease); }
.fin-ap-page .adv-link:hover, .fin-ap-page .adv-link.is-open { color: var(--brand-600, #4f46e5); }
.fin-ap-page .adv-link .chev { transition: transform .2s var(--ease-out, ease); }
.fin-ap-page .adv-link.is-open .chev { transform: rotate(180deg); }
.fin-ap-page .filter-card .filter-bar { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px 16px; }
.fin-ap-page .filter-card .field { display: flex; flex-direction: column; gap: 6px; min-width: 0; }
.fin-ap-page .filter-card .field > label { font-size: 13px; color: var(--ink-500, #6b7280); font-weight: 500; }
.fin-ap-page .filter-card .control { min-width: 0; }
.fin-ap-page .filter-card .control.is-select :deep(.el-select) { width: 100%; }
.fin-ap-page .filter-card .filter-actions {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 14px; padding-top: 12px;
  border-top: 1px dashed var(--ink-200, #e5e7eb);
}
.fin-ap-page .filter-card .filter-info { display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--ink-500, #6b7280); }
.fin-ap-page .filter-card .filter-buttons { display: flex; align-items: center; gap: 8px; }

/* ===== 工具栏 / 表格 ===== */
.fin-ap-page .toolbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-bottom: 1px solid var(--ink-200, #e5e7eb); background: var(--ink-50, #f9fafb); }
.fin-ap-page .toolbar .left { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.fin-ap-page .toolbar .right { display: flex; gap: 8px; align-items: center; }
.fin-ap-page .table-wrap { overflow-x: auto; }
.fin-ap-page .app-table { --el-table-bg-color: #fff; --el-table-header-bg-color: var(--ink-50, #f9fafb); --el-table-row-hover-bg-color: #fafbff; --el-table-border-color: var(--ink-200, #e5e7eb); --el-table-text-color: var(--ink-700, #374151); --el-table-header-text-color: var(--ink-500, #6b7280); }
.fin-ap-page .el-table :deep(.el-table__header th) { background: var(--ink-50, #f9fafb) !important; color: var(--ink-500, #6b7280); font-weight: 600; font-size: 14px; letter-spacing: .02em; padding: 12px 16px; border-bottom: 1px solid var(--ink-200, #e5e7eb); }
.fin-ap-page .el-table :deep(.el-table__body td) { padding: 14px 16px; border-bottom: 1px solid var(--ink-100, #f3f4f6); color: var(--ink-700, #374151); }
.fin-ap-page .el-table :deep(.el-table__row:hover > td) { background: #fafbff !important; }
.fin-ap-page .el-table .col-mono { font-family: ui-monospace, "JetBrains Mono", "SF Mono", Menlo, monospace; font-size: 14px; color: var(--ink-700, #374151); letter-spacing: -.01em; }
.fin-ap-page .el-table .rd-amount { font-feature-settings: "tnum" 1; font-variant-numeric: tabular-nums; color: #dc2626; font-weight: 600; }

/* ===== 状态徽章（dot + 文字） ===== */
.fin-ap-page .badge { display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 999px; font-size: 13px; font-weight: 600; line-height: 1; border: 1px solid transparent; }
.fin-ap-page .badge .dot { width: 6px; height: 6px; border-radius: 50%; }
.fin-ap-page .badge.badge-amber { background: var(--amber-50, #fffbeb); color: var(--amber-700, #b45309); border-color: #fde68a; }
.fin-ap-page .badge.badge-amber .dot { background: var(--amber-500, #f59e0b); }
.fin-ap-page .badge.badge-blue { background: var(--blue-50, #eff6ff); color: var(--blue-700, #1d4ed8); border-color: #bfdbfe; }
.fin-ap-page .badge.badge-blue .dot { background: var(--blue-500, #3b82f6); }
.fin-ap-page .badge.badge-green { background: var(--green-50, #ecfdf5); color: var(--green-700, #047857); border-color: #a7f3d0; }
.fin-ap-page .badge.badge-green .dot { background: var(--green-500, #10b981); }
.fin-ap-page .badge.badge-gray { background: var(--ink-100, #f3f4f6); color: var(--ink-500, #6b7280); border-color: var(--ink-200, #e5e7eb); }
.fin-ap-page .badge.badge-gray .dot { background: var(--ink-400, #9ca3af); }

/* ===== 分页 ===== */
.fin-ap-page .pagination-container { display: flex; align-items: center; justify-content: flex-end; padding: 14px 20px; font-size: 14px; color: var(--ink-500, #6b7280); background: #fff; }
.fin-ap-page .pagination-container :deep(.el-pagination) { justify-content: flex-end; }
.fin-ap-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius: 6px; border: 1px solid var(--ink-200, #e5e7eb); background: #fff; min-width: 32px; height: 32px; line-height: 32px; font-size: 14px; color: var(--ink-700, #374151); margin: 0 2px; }
.fin-ap-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background: var(--brand-600, #4f46e5); border-color: var(--brand-600, #4f46e5); color: #fff; font-weight: 600; box-shadow: 0 4px 10px -2px rgba(79, 70, 229, .4); }
.fin-ap-page .pagination-container :deep(.el-pagination .btn-prev), .fin-ap-page .pagination-container :deep(.el-pagination .btn-next) { border-radius: 6px; border: 1px solid var(--ink-200, #e5e7eb); background: #fff; min-width: 32px; height: 32px; }

/* ===== 操作列 ===== */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== 响应式 ===== */
@media (max-width: 1100px) { .fin-ap-page .filter-card .filter-bar { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 720px) { .fin-ap-page .filter-card .filter-bar { grid-template-columns: 1fr; } .fin-ap-page .toolbar { flex-wrap: wrap; gap: 10px; } }
</style>