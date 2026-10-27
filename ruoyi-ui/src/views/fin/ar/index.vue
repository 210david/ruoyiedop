<template>
  <div class="app-container fin-ar-page">
    <!-- 应收总览（stat-card 样式，对齐回款管理页面） -->
    <el-row :gutter="12" class="mb8">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">应收总额（元）</div>
          <div class="stat-value">{{ formatAmount(summary.totalAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-success">
          <div class="stat-label">已收（元）</div>
          <div class="stat-value">{{ formatAmount(summary.receivedAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-warning">
          <div class="stat-label">未收（元）</div>
          <div class="stat-value">{{ formatAmount(summary.unreceivedAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-danger">
          <div class="stat-label">逾期未付</div>
          <div class="stat-value">{{ overdueList.length }}<span class="stat-unit">笔</span></div>
        </div>
      </el-col>
    </el-row>

    <!-- 筛选卡片 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span>筛选条件</div>
        <div class="adv-link" :class="{ 'is-open': showAdvanced }" @click="showAdvanced = !showAdvanced">
          高级筛选<el-icon><ArrowDown /></el-icon>
        </div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>应收单号</label>
          <div class="control">
            <el-input v-model="queryParams.arNo" placeholder="请输入应收单号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>客户</label>
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
          <label>应收日期</label>
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
        <div class="field" v-show="showAdvanced">
          <label>会计期间</label>
          <div class="control">
            <el-select v-model="queryParams.period" placeholder="全部" clearable class="control is-select" @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="p in periodOptions" :key="p" :label="formatPeriod(p)" :value="p" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- 数据卡片 -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button v-hasPermi="['fin:ar:add']" type="primary" plain icon="Plus" @click="handleGenerate">从发货单生成应收单</el-button>
          <el-button v-hasPermi="['fin:ar:receipt']" type="warning" icon="Refresh" @click="handleSyncReceived">同步已收金额</el-button>
          <el-button v-hasPermi="['fin:ar:badDebt']" type="danger" icon="Warning" @click="handleBadDebt">坏账计提</el-button>
          <el-button icon="DataAnalysis" @click="showAging = true">账龄分析</el-button>
          <el-button icon="Warning" @click="showOverdue = true">逾期预警</el-button>
          <el-button v-hasPermi="['fin:ar:export']" icon="Download" @click="handleExport">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:show-search="showSearch" :columns="columns" storageKey="fin_ar_columns" @query-table="getArList" />
          <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
        </div>
      </div>

      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>
          <span class="hint-text">
            应收单由<b>已审核的发货单</b>自动生成，金额不可手工修改；
            已收金额以营销系统收款记录为准，财务侧不重复登记，避免重复收款。
            税额按配置的<b>销售税率</b>（默认13%）从价税合计自动拆分。
          </span>
        </template>
      </el-alert>

      <div class="table-wrap">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="arList"
          border
          highlight-current
          @header-dragend="onHeaderDragEnd"
          @current-change="handleCurrentChange">
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column v-if="colVisible('apNo')" key="apNo" prop="arNo" label="应收单号" align="center" resizable :min-width="colWidth('apNo', 150)" show-overflow-tooltip>
            <template #default="{ row }"><span class="col-mono">{{ row.arNo }}</span></template>
          </el-table-column>
          <el-table-column v-if="colVisible('partnerName')" key="partnerName" prop="partnerName" label="客户" align="center" resizable :min-width="colWidth('partnerName', 180)" show-overflow-tooltip />
          <el-table-column v-if="colVisible('sourceKey')" key="sourceKey" prop="sourceKey" label="来源发货单" align="center" resizable :width="colWidth('sourceKey', 140)">
            <template #default="{ row }">
              <el-tooltip :content="`来源：${row.sourceModule} / ${row.sourceType}`">
                <span class="text-muted">{{ row.sourceKey || '手工' }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column v-if="colVisible('apDate')" key="apDate" prop="apDate" label="应收日期" align="center" resizable :width="colWidth('apDate', 110)">
            <template #default="{ row }">{{ row.apDate ? String(row.apDate).slice(0, 10) : '—' }}</template>
          </el-table-column>
          <el-table-column v-if="colVisible('amountExclTax')" key="amountExclTax" prop="amountExclTax" label="不含税" align="center" resizable :width="colWidth('amountExclTax', 120)">
            <template #default="{ row }">{{ formatAmount(row.amountExclTax) }}</template>
          </el-table-column>
          <el-table-column v-if="colVisible('taxAmount')" key="taxAmount" prop="taxAmount" label="税额" align="center" resizable :width="colWidth('taxAmount', 110)">
            <template #default="{ row }">{{ formatAmount(row.taxAmount) }}</template>
          </el-table-column>
          <el-table-column v-if="colVisible('totalAmount')" key="totalAmount" prop="totalAmount" label="价税合计" align="center" resizable :width="colWidth('totalAmount', 130)">
            <template #default="{ row }"><span class="rd-amount">{{ formatAmount(row.totalAmount) }}</span></template>
          </el-table-column>
          <el-table-column v-if="colVisible('receivedAmount')" key="receivedAmount" prop="receivedAmount" label="已收" align="center" resizable :width="colWidth('receivedAmount', 120)">
            <template #default="{ row }"><span class="text-received">{{ formatAmount(row.receivedAmount) }}</span></template>
          </el-table-column>
          <el-table-column v-if="colVisible('unreceivedAmount')" key="unreceivedAmount" prop="unreceivedAmount" label="未付" align="center" resizable :width="colWidth('unreceivedAmount', 130)">
            <template #default="{ row }">
              <span class="amount-cell strong text-unreceived">
                {{ formatAmount(Number(row.totalAmount || 0) - Number(row.receivedAmount || 0)) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column v-if="colVisible('dueDate')" key="dueDate" prop="dueDate" label="到期日" align="center" resizable :width="colWidth('dueDate', 110)">
            <template #default="{ row }">
              <span :class="isOverdue(row) ? 'text-danger' : 'text-muted'">
                {{ row.dueDate ? String(row.dueDate).slice(0, 10) : '—' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column v-if="colVisible('status')" key="status" prop="status" label="状态" align="center" resizable :width="colWidth('status', 100)">
            <template #default="{ row }">
              <span class="badge" :class="badgeClass(row.status)"><span class="dot"></span>{{ statusLabel(row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="{ row }">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(row)">详情</el-button>
                <el-button v-if="canRowWriteoff(row)" v-hasPermi="['fin:ar:writeoff']"
                           link type="success" icon="CircleCheck" @click="handleRowWriteoff(row)">核销</el-button>
                <el-button v-if="canRowInvoice(row)" v-hasPermi="['fin:ar:invoice']"
                           link type="primary" icon="Tickets" @click="handleRowInvoice(row)">登记发票</el-button>
                <el-button v-if="row.status !== 'SETTLED' && row.status !== 'VOID'"
                           v-hasPermi="['fin:ar:edit']" link type="warning" icon="Close" @click="handleVoid(row)">作废</el-button>
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

    <!-- 详情弹框（独立只读） -->
    <el-dialog v-model="viewOpen" class="rd-dialog" width="820px" append-to-body>
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="20"><Tickets /></el-icon></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">应收单详情</div>
            <div class="rd-detail-header-sub">
              <span class="rd-detail-header-no">{{ current?.arNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page" v-if="current">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><el-icon><Document /></el-icon></span>基本信息
            </div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">客户</span><span class="rd-value">{{ current.partnerName }}</span></div>
              <div class="rd-item"><span class="rd-label">来源发货单</span><span class="rd-value">{{ current.sourceKey || '手工' }}</span></div>
              <div class="rd-item"><span class="rd-label">发票号码</span><span class="rd-value">{{ current.invoiceNo || '—' }}</span></div>
              <div class="rd-item"><span class="rd-label">应收日期</span><span class="rd-value">{{ current.apDate }}</span></div>
              <div class="rd-item"><span class="rd-label">会计期间</span><span class="rd-value">{{ formatPeriod(current.period) }}</span></div>
              <div class="rd-item"><span class="rd-label">账期</span><span class="rd-value">{{ current.paymentDays }} 天</span></div>
              <div class="rd-item"><span class="rd-label">不含税金额</span><span class="rd-value rd-amount">{{ formatAmount(current.amountExclTax) }}</span></div>
              <div class="rd-item"><span class="rd-label">税额</span><span class="rd-value rd-amount">{{ formatAmount(current.taxAmount) }}</span></div>
              <div class="rd-item"><span class="rd-label">价税合计</span><span class="rd-value rd-amount rd-value--large">{{ formatAmount(current.totalAmount) }}</span></div>
              <div class="rd-item"><span class="rd-label">已收金额</span><span class="rd-value rd-amount">{{ formatAmount(current.receivedAmount) }}</span></div>
              <div class="rd-item"><span class="rd-label">未付余额</span><span class="rd-value rd-amount rd-value--large">{{ formatAmount(detailUnreceived) }}</span></div>
              <div class="rd-item"><span class="rd-label">到期日</span><span class="rd-value">{{ current.dueDate || '—' }}</span></div>
              <div class="rd-item"><span class="rd-label">状态</span><span class="rd-value"><span class="badge" :class="badgeClass(current.status)"><span class="dot"></span>{{ statusLabel(current.status) }}</span></span></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><span class="rd-value">{{ current.remark || '—' }}</span></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><el-icon><CircleCheck /></el-icon></span>核销记录
            </div>
          </div>
          <div class="rd-card-body">
            <el-table :data="writeoffList" size="small" border>
              <el-table-column prop="writeoffDate" label="核销日期" width="120" align="center" />
              <el-table-column label="核销金额" width="140" align="center">
                <template #default="{ row }">
                  <span class="rd-amount">{{ formatAmount(row.writeoffAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="createBy" label="核销人" width="120" align="center" />
              <el-table-column prop="createTime" label="核销时间" min-width="160" align="center" />
              <el-table-column prop="remark" label="备注" min-width="140" align="center" />
              <template #empty>
                <div class="rd-empty"><span class="rd-empty-text">尚未核销</span></div>
              </template>
            </el-table>
          </div>
        </section>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="viewOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 核销弹框 -->
    <el-dialog v-model="showWriteoff" width="480px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="20"><CircleCheck /></el-icon></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">收款核销</div>
            <div class="rd-detail-header-sub">
              <span class="rd-detail-header-no">{{ current?.arNo || '' }}</span>
            </div>
          </div>
        </div>
      </template>
      <el-form label-width="90px">
        <el-form-item label="应收单">
          <el-input :model-value="current?.arNo" disabled />
        </el-form-item>
        <el-form-item label="未付余额">
          <el-input :model-value="formatAmount(detailUnreceived)" disabled />
        </el-form-item>
        <el-form-item label="核销金额" required>
          <el-input v-model="writeoffAmount" type="number" :max="detailUnreceived" placeholder="请输入核销金额" />
        </el-form-item>
        <el-form-item label="快速填写">
          <el-button size="small" @click="writeoffAmount = detailUnreceived">全额 {{ formatAmount(detailUnreceived) }}</el-button>
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
            <div class="rd-detail-header-title">应收单生成结果</div>
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

    <!-- 销项发票登记 -->
    <el-dialog v-model="showInvoice" width="480px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="20"><Tickets /></el-icon></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">登记销项发票</div>
            <div class="rd-detail-header-sub">
              <span class="rd-detail-header-no">{{ current?.arNo || '' }}</span>
            </div>
          </div>
        </div>
      </template>
      <el-form label-width="90px">
        <el-form-item label="应收单号">
          <el-input :model-value="current?.arNo" disabled />
        </el-form-item>
        <el-form-item label="发票号码" required>
          <el-input v-model="invoiceForm.invoiceNo" placeholder="请输入销项发票号码" />
        </el-form-item>
        <el-form-item label="发票代码">
          <el-input v-model="invoiceForm.invoiceCode" placeholder="选填" />
        </el-form-item>
        <el-form-item label="开票日期">
          <el-date-picker v-model="invoiceForm.invoiceDate" type="date" value-format="YYYY-MM-DD"
                          style="width: 100%" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitInvoice">确 定</el-button>
          <el-button @click="showInvoice = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 账龄分析 -->
    <el-dialog v-model="showAging" width="720px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="20"><DataAnalysis /></el-icon></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">应收账龄分析</div>
            <div class="rd-detail-header-sub">
              <span class="rd-detail-header-no">按账龄区间统计未收款项</span>
            </div>
          </div>
        </div>
      </template>
      <div style="margin-bottom: 12px">
        <el-button type="primary" icon="Search" @click="loadAging">查询</el-button>
      </div>
      <el-table :data="agingList" size="small" border>
        <el-table-column prop="bucket" label="账龄区间" width="180" align="center" />
        <el-table-column prop="cnt" label="笔数" width="100" align="center" />
        <el-table-column label="应收总额" width="150" align="center">
          <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="未收金额" width="150" align="center">
          <template #default="{ row }">
            <span class="text-unreceived">{{ formatAmount(row.unreceivedAmount) }}</span>
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
              <span class="rd-detail-header-no">已到期仍未收清的应收单</span>
            </div>
          </div>
        </div>
      </template>
      <el-table :data="overdueList" size="small" border max-height="420">
        <el-table-column prop="arNo" label="应收单号" width="150" align="center" />
        <el-table-column prop="partnerName" label="客户" min-width="180" align="center" />
        <el-table-column prop="dueDate" label="到期日" width="110" align="center" />
        <el-table-column label="未收金额" width="140" align="center">
          <template #default="{ row }">
            <span class="text-danger">{{ formatAmount(Number(row.totalAmount || 0) - Number(row.receivedAmount || 0)) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="账龄" width="100" align="center">
          <template #default="{ row }">{{ overdueDays(row) }} 天</template>
        </el-table-column>
      </el-table>
    </el-dialog>
    <el-dialog v-model="showStatusHelp" title="应收管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content"><h4>一、状态流转图</h4><div class="status-flow"><el-tag type="info">待收</el-tag><span class="flow-arrow">→</span><el-tag type="primary">部分收款</el-tag><span class="flow-arrow">→</span><el-tag type="success">已收清</el-tag><span class="flow-arrow">→</span><el-tag type="warning">已核销</el-tag></div><h4>二、各状态说明</h4><el-descriptions :column="1" border><el-descriptions-item label="待收">应收单已生成，尚未收到任何回款，可跟踪收款进度</el-descriptions-item><el-descriptions-item label="部分收款">已收到部分回款，剩余应收金额继续跟踪</el-descriptions-item><el-descriptions-item label="已收清">应收金额全部收回，单据收款完成</el-descriptions-item><el-descriptions-item label="已核销">应收单与回款完成核销匹配，往来账务闭环</el-descriptions-item></el-descriptions><h4>三、重点业务规则</h4><el-row :gutter="16"><el-col :span="12"><div class="highlight-card highlight-primary"><div class="highlight-card-title">发货自动生成应收单</div><div class="highlight-card-body">从销售发货单生成应收单，系统自动<strong>编号（AR 前缀）</strong>并关联客户与发货单</div></div></el-col><el-col :span="12"><div class="highlight-card highlight-warning"><div class="highlight-card-title">同步已收金额</div><div class="highlight-card-body">回款完成后点击「同步已收金额」刷新实收数据；<strong>未同步前</strong>单据状态不会变化</div></div></el-col></el-row><el-row :gutter="16" style="margin-top: 12px;"><el-col :span="12"><div class="highlight-card highlight-success"><div class="highlight-card-title">坏账计提</div><div class="highlight-card-body">点击「坏账计提」按规则计提坏账准备，<strong>自动生成计提单号（BD 前缀）</strong>并生成凭证</div></div></el-col><el-col :span="12"><div class="highlight-card highlight-danger"><div class="highlight-card-title">账龄分析</div><div class="highlight-card-body">账龄分析按客户与账期区间统计应收余额，<strong>超期应收一目了然</strong></div></div></el-col></el-row><h4>四、业务操作流程</h4><el-timeline><el-timeline-item type="primary" :hollow="true"><strong>生成应收单：</strong>点击「从发货单生成应收单」，选择销售发货单自动生成，核对金额与客户</el-timeline-item><el-timeline-item type="warning" :hollow="true"><strong>收款跟踪：</strong>客户回款后点击「同步已收金额」，更新实收金额与单据状态</el-timeline-item><el-timeline-item type="success" :hollow="true"><strong>应收核销：</strong>单据收清后执行核销，冲抵往来欠款</el-timeline-item><el-timeline-item type="info" :hollow="true"><strong>坏账计提：</strong>对预计无法收回的应收按规则计提坏账，生成计提单与凭证</el-timeline-item></el-timeline></div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="FinAr">
const showStatusHelp = ref(false)
import {
  listAr, getAr, generateAr, syncReceived, writeoffAr, voidAr, getArAging, getArOverdue, getArSummary,
  invoiceAr, badDebt
} from '@/api/fin/ar'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('fin_ar_index')

const loading = ref(false)
const showSearch = ref(true)
const arList = ref([])
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
const showInvoice = ref(false)
const invoiceForm = reactive({ invoiceNo: '', invoiceCode: '', invoiceDate: '' })
const badDebtPeriod = ref('')
const showResult = ref(false)
const showAging = ref(false)
const showOverdue = ref(false)
const writeoffAmount = ref('')
const writeoffRemark = ref('')

/** 列显隐配置 */
const defaultColumns = [
  { key: 'apNo', label: '应收单号', visible: true },
  { key: 'partnerName', label: '客户', visible: true },
  { key: 'sourceKey', label: '来源发货单', visible: true },
  { key: 'apDate', label: '应收日期', visible: true },
  { key: 'amountExclTax', label: '不含税', visible: true },
  { key: 'taxAmount', label: '税额', visible: true },
  { key: 'totalAmount', label: '价税合计', visible: true },
  { key: 'receivedAmount', label: '已收', visible: true },
  { key: 'unreceivedAmount', label: '未付', visible: true },
  { key: 'dueDate', label: '到期日', visible: true },
  { key: 'status', label: '状态', visible: true }
]
const columns = ref(defaultColumns.map(c => ({ ...c })))

/** 列显隐辅助（columns 为数组，模板不能按对象访问） */
function colVisible(key) {
  const c = columns.value.find(x => x.key === key)
  return c ? c.visible !== false : true
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('fin_ar_columns')
    if (saved) {
      const arr = JSON.parse(saved)
      columns.value.forEach(col => {
        const s = arr.find(item => item.key === col.key)
        if (s) col.visible = !!s.visible
      })
    }
  } catch (e) { /* ignore */ }
}
loadColumnVisibility()

const queryParams = ref({ pageNum: 1, pageSize: 20, arNo: undefined, partnerName: undefined, status: undefined, period: undefined })

const showAdvanced = ref(false)

const activeFilterCount = computed(() => {
  const q = queryParams.value
  let n = 0
  if (q.arNo) n++
  if (q.partnerName) n++
  if (q.status) n++
  if (q.period) n++
  if (dateRange.value && dateRange.value.length === 2) n++
  return n
})

/** 近 24 个自然月（yyyyMM） */
const periodOptions = computed(() => {
  const list = []
  const d = new Date()
  for (let i = 0; i < 24; i++) {
    const dd = new Date(d.getFullYear(), d.getMonth() - i, 1)
    list.push(`${dd.getFullYear()}${String(dd.getMonth() + 1).padStart(2, '0')}`)
  }
  return list
})

const detailUnreceived = computed(() => {
  if (!current.value) return 0
  return Number(current.value.totalAmount || 0) - Number(current.value.receivedAmount || 0)
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
  return { OPEN: 'badge-amber', PARTIAL: 'badge-violet', SETTLED: 'badge-green', VOID: 'badge-gray' }[s] || 'badge-gray'
}

function isOverdue(row) {
  if (!row.dueDate || ['SETTLED', 'VOID'].includes(row.status)) return false
  if (Number(row.totalAmount || 0) - Number(row.receivedAmount || 0) <= 0) return false
  return new Date(row.dueDate) < new Date()
}

function overdueDays(row) {
  if (!row.dueDate) return 0
  return Math.floor((Date.now() - new Date(row.dueDate).getTime()) / 86400000)
}

function getArList() {
  loading.value = true
  const params = { ...queryParams.value }
  if (dateRange.value && dateRange.value.length === 2) {
    params.params = { beginArDate: dateRange.value[0], endArDate: dateRange.value[1] }
  }
  listAr(params).then(res => {
    arList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    applySavedWidths()
  })
}

function loadSummary() {
  getArSummary().then(res => { summary.value = res.data || {} })
}

function loadOverdue() {
  getArOverdue().then(res => { overdueList.value = res.data || [] })
}

function handleQuery() { queryParams.value.pageNum = 1; getArList() }

function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: 20, arNo: undefined, partnerName: undefined, status: undefined, period: undefined }
  dateRange.value = []
  getArList()
}

/** 行选中：同步为当前核销对象 */

function handleCurrentChange(row) { current.value = row }

/** 从发货单生成应收单 */
function handleGenerate() {
  proxy.$modal.confirm('将扫描已审核的发货单生成应收单，是否继续？').then(() => {
    return generateAr()
  }).then(res => {
    lastResult.value = res.data
    showResult.value = true
    getArList(); loadSummary(); loadOverdue()
  }).catch(() => {})
}

/** 同步已收金额 */
function handleSyncReceived() {
  proxy.$modal.confirm('将从营销系统同步最新的已收金额，是否继续？').then(() => {
    return syncReceived()
  }).then(res => {
    proxy.$modal.msgSuccess(`已同步 ${res.data} 张应收单的已收金额`)
    getArList(); loadSummary(); loadOverdue()
  }).catch(() => {})
}

/** 登记销项发票 */
/** 行级发票登记：未作废的应收单可登记销项发票 */
function canRowInvoice(row) {
  return !!row && row.status !== 'VOID'
}

function handleRowInvoice(row) {
  current.value = row
  invoiceForm.invoiceNo = row.invoiceNo || ''
  invoiceForm.invoiceCode = row.invoiceCode || ''
  invoiceForm.invoiceDate = row.invoiceDate
    ? String(row.invoiceDate).slice(0, 10) : ''
  showInvoice.value = true
}

function submitInvoice() {
  if (!invoiceForm.invoiceNo || !invoiceForm.invoiceNo.trim()) {
    proxy.$modal.msgError('销项发票号码不能为空')
    return
  }
  invoiceAr(current.value.arId, invoiceForm.invoiceNo.trim(),
            invoiceForm.invoiceCode, invoiceForm.invoiceDate).then(() => {
    proxy.$modal.msgSuccess('销项发票登记成功')
    showInvoice.value = false
    getArList()
  })
}

/** 坏账计提 */
function handleBadDebt() {
  const d = new Date()
  badDebtPeriod.value = `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}`
  proxy.$modal.confirm(`将按账龄区间比例对${badDebtPeriod.value} 期间的逾期应收计提坏账（1-30天5%、31-60天10%、61-90天30%、90天以上50%），是否继续？`).then(() => {
    return badDebt(badDebtPeriod.value)
  }).then(res => {
    const d2 = res.data
    if (d2.count === 0) {
      proxy.$modal.msgInfo('本期无逾期应收，无需计提')
    } else {
      proxy.$modal.msgSuccess(`计提 ${d2.count} 笔，合计 ${formatAmount(d2.totalAmount)} 元`)
    }
    getArList(); loadSummary()
  }).catch(() => {})
}

/** 查看详情（独立只读弹窗） */
function handleView(row) {
  getAr(row.arId).then(res => {
    current.value = res.data.ar
    writeoffList.value = res.data.writeoffs || []
    viewOpen.value = true
  })
}

/** 行级核销：未核销/部分核销且有未收余额的应收单可核销 */
function canRowWriteoff(row) {
  if (!row || ['SETTLED', 'VOID'].includes(row.status)) return false
  return Number(row.totalAmount || 0) - Number(row.receivedAmount || 0) > 0
}

function handleRowWriteoff(row) {
  current.value = row
  writeoffAmount.value = (Number(row.totalAmount || 0) - Number(row.receivedAmount || 0)).toFixed(2)
  writeoffRemark.value = ''
  showWriteoff.value = true
}

function submitWriteoff() {
  const amt = Number(writeoffAmount.value)
  if (!amt || amt <= 0) {
    proxy.$modal.msgError('核销金额必须大于 0')
    return
  }
  if (amt > detailUnreceived.value) {
    proxy.$modal.msgError(`核销金额不能超过未付余额 ${formatAmount(detailUnreceived.value)}`)
    return
  }
  writeoffAr(current.value.arId, amt, writeoffRemark.value).then(() => {
    proxy.$modal.msgSuccess('核销成功')
    showWriteoff.value = false
    getArList(); loadSummary(); loadOverdue()
    // 刷新当前单据，避免详情弹框显示过期数据
    if (current.value && current.value.arId) {
      getAr(current.value.arId).then(res => {
        current.value = res.data.ar
        writeoffList.value = res.data.writeoffs || []
      })
    }
  })
}

function handleVoid(row) {
  proxy.$modal.confirm(`是否确认作废应收单「${row.arNo}」？作废后不再参与账龄统计。`).then(() => {
    return voidAr(row.arId, '手工作废')
  }).then(() => {
    proxy.$modal.msgSuccess('作废成功')
    getArList(); loadSummary(); loadOverdue()
  }).catch(() => {})
}

function loadAging() {
  getArAging().then(res => { agingList.value = res.data || [] })
}

function handleExport() {
  proxy.download('/fin/ap/export', { ...queryParams.value }, `应收单_${new Date().getTime()}.xlsx`)
}

getArList()
loadSummary()
loadOverdue()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.fin-ar-page {
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
.fin-ar-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Summary Bar ===== */
/* ===== stat-card（对齐回款管理页面） ===== */
.fin-ar-page .stat-card {
  background:#fff;
  border:1px solid var(--ink-200);
  border-radius:var(--r-lg);
  box-shadow:var(--shadow-card);
  padding:16px 18px;
  text-align:center;
  transition:all .15s var(--ease-out);
}
.fin-ar-page .stat-card:hover {
  border-color:var(--brand-200);
  box-shadow:0 4px 12px -2px rgba(99,102,241,.12);
}
.fin-ar-page .stat-card .stat-label { font-size:13px; color:var(--ink-500); margin-bottom:8px; }
.fin-ar-page .stat-card .stat-value { font-size:22px; font-weight:700; color:var(--ink-900); font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; }
.fin-ar-page .stat-card.stat-success .stat-value { color:var(--green-500); }
.fin-ar-page .stat-card.stat-warning .stat-value { color:var(--amber-500); }
.fin-ar-page .stat-card.stat-danger .stat-value { color:var(--red-500); }
.fin-ar-page .stat-unit { font-size:12px; margin-left:2px; font-weight:400; }

/* ===== Filter Card ===== */
.fin-ar-page .filter-card { padding:14px 20px 16px; }
.fin-ar-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.fin-ar-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.fin-ar-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.fin-ar-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.fin-ar-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.fin-ar-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.fin-ar-page .filter-card .filter-buttons { display:flex; gap:8px; }
.fin-ar-page .adv-link { display:flex; align-items:center; gap:4px; font-size:13px; color:var(--brand-600); cursor:pointer; user-select:none; transition:color .15s var(--ease-out); }
.fin-ar-page .adv-link:hover { color:var(--brand-700); }
.fin-ar-page .adv-link .el-icon { transition:transform .2s var(--ease-out); }
.fin-ar-page .adv-link.is-open .el-icon { transform:rotate(180deg); }

/* ===== Form Field ===== */
.fin-ar-page .field { display:flex; flex-direction:column; gap:6px; }
.fin-ar-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.fin-ar-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.fin-ar-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.fin-ar-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.fin-ar-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.fin-ar-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.fin-ar-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.fin-ar-page .field .control :deep(.el-select) { width:100%; }
.fin-ar-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.fin-ar-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.fin-ar-page .field .control :deep(.el-date-editor) { width:100%; }
.fin-ar-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.fin-ar-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.fin-ar-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Toolbar ===== */
.fin-ar-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.fin-ar-page .toolbar .left { display:flex; gap:8px; align-items:center; flex-wrap:wrap; }
.fin-ar-page .toolbar .right { display:flex; gap:8px; align-items:center; }

/* ===== Table ===== */
.fin-ar-page .table-wrap { overflow-x:auto; }
.fin-ar-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:var(--ink-200); --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.fin-ar-page .el-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.fin-ar-page .el-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.fin-ar-page .el-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.fin-ar-page .el-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.fin-ar-page .el-table .rd-amount { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:#dc2626; font-weight:600; }
.fin-ar-page .amount-cell { font-variant-numeric:tabular-nums; }
.fin-ar-page .amount-cell.strong { font-weight:600; }
.fin-ar-page .text-muted { color:var(--ink-400); }
.fin-ar-page .text-received { color:var(--green-700); }
.fin-ar-page .text-unreceived { color:#b85450; }
.fin-ar-page .text-success { color:var(--green-700); font-weight:600; }
.fin-ar-page .text-danger { color:var(--red-700); font-weight:600; }

/* ===== Badges ===== */
.fin-ar-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.fin-ar-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.fin-ar-page .badge.badge-amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.fin-ar-page .badge.badge-amber .dot { background:var(--amber-500); }
.fin-ar-page .badge.badge-blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.fin-ar-page .badge.badge-blue .dot { background:var(--blue-500); }
.fin-ar-page .badge.badge-green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.fin-ar-page .badge.badge-green .dot { background:var(--green-500); }
.fin-ar-page .badge.badge-red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.fin-ar-page .badge.badge-red .dot { background:var(--red-500); }
.fin-ar-page .badge.badge-violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.fin-ar-page .badge.badge-violet .dot { background:var(--brand-500); }
.fin-ar-page .badge.badge-gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.fin-ar-page .badge.badge-gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.fin-ar-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
.fin-ar-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.fin-ar-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.fin-ar-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.fin-ar-page .pagination-container :deep(.el-pagination .btn-prev), .fin-ar-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .fin-ar-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .fin-ar-page .filter-card .filter-bar { grid-template-columns:1fr; } .fin-ar-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* ===== 操作列 ===== */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== 详情弹窗（rd- 规范） ===== */
:deep(.rd-dialog .el-dialog__header) { padding:0; margin:0; border:none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top:14px; right:14px; z-index:10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color:#fff; font-size:20px; }
:deep(.rd-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color:#fff; }
:deep(.rd-dialog .el-dialog__body) { padding:12px 16px 16px; background:linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }
:deep(.rd-dialog .rd-page) { max-width:none; }

.rd-detail-header { display:flex; align-items:center; gap:12px; padding:12px 20px; background:linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius:12px 12px 0 0; position:relative; overflow:hidden; }
.rd-detail-header::before { content:''; position:absolute; top:-30px; right:-15px; width:140px; height:140px; border-radius:50%; background:radial-gradient(circle, rgb(255 255 255 / 0.15) 0%, transparent 70%); pointer-events:none; }
.rd-detail-header-icon { display:flex; align-items:center; justify-content:center; width:40px; height:40px; border-radius:10px; background:rgb(255 255 255 / 0.2); border:1px solid rgb(255 255 255 / 0.25); color:#fff; flex-shrink:0; backdrop-filter:blur(4px); box-shadow:0 4px 12px rgb(0 0 0 / 0.1); }
.rd-detail-header-main { display:flex; flex-direction:column; gap:4px; min-width:0; padding-right:36px; }
.rd-detail-header-title { font-size:18px; font-weight:700; color:#fff; letter-spacing:-0.02em; line-height:1.3; }
.rd-detail-header-sub { display:flex; align-items:center; gap:12px; flex-wrap:wrap; }
.rd-detail-header-no { font-size:13px; font-weight:500; color:rgb(255 255 255 / 0.8); font-variant-numeric:tabular-nums; }

.rd-page { max-width:840px; margin:0 auto; }
.rd-card { background:#fff; border-radius:12px; border:1px solid #e5e7eb; box-shadow:0 1px 2px 0 rgb(0 0 0 / 0.05); overflow:hidden; margin-bottom:8px; transition:box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1); animation:rdFadeIn 0.4s ease-out forwards; }
.rd-card:hover { box-shadow:0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1); }
.rd-card:last-child { margin-bottom:0; }
.rd-card-header { display:flex; align-items:center; justify-content:space-between; padding:8px 16px; background:linear-gradient(to right, #f9fafb, #fff); border-bottom:1px solid #f3f4f6; }
.rd-card-title { display:flex; align-items:center; gap:8px; font-size:16px; font-weight:700; color:#111827; }
.rd-card-icon { display:flex; align-items:center; justify-content:center; width:32px; height:32px; border-radius:8px; background:#fff; border:1px solid #e5e7eb; color:#2563eb; box-shadow:0 1px 2px 0 rgb(0 0 0 / 0.05); }
.rd-card-body { padding:14px 16px; }
.rd-grid { display:grid; grid-template-columns:repeat(2, 1fr); gap:12px 24px; }
.rd-item { display:flex; align-items:center; gap:12px; }
.rd-item--full { grid-column:1 / -1; }
.rd-label { flex:0 0 auto; min-width:72px; display:flex; align-items:center; font-size:14px; font-weight:500; color:#6b7280; white-space:nowrap; }
.rd-value { flex:1 1 auto; font-size:14px; font-weight:500; color:#111827; line-height:1.5; padding-left:12px; border-left:1px solid #e5e7eb; min-width:0; }
.rd-value--large { font-size:18px; font-weight:700; }
.rd-amount { font-variant-numeric:tabular-nums; font-weight:700; color:#dc2626; }
.rd-empty { display:flex; flex-direction:column; align-items:center; justify-content:center; padding:20px; text-align:center; color:#9ca3af; background:#f9fafb; border-radius:12px; border:1px dashed #e5e7eb; }
.rd-empty-text { font-size:14px; font-weight:500; margin:0; }
@keyframes rdFadeIn { from { opacity:0; transform:translateY(8px); } to { opacity:1; transform:translateY(0); } }
.rd-card:nth-child(2) { animation-delay:0.06s; }
.rd-card:nth-child(3) { animation-delay:0.12s; }
@media (max-width:768px) {
  .rd-grid { grid-template-columns:1fr; }
  .rd-card-header { padding:8px 12px; }
  .rd-card-body { padding:12px; }
}

.hint-text { font-size:12px; }
.divider-text { font-size:13px; }
</style>
