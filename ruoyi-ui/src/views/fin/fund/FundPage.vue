<template>
  <div class="app-container fin-list-page fin-fund-page">
      <!-- 菜单重构：页签已独立为菜单，按路由分区展示（资金账户 / 资金流水 / 银行对账） -->
        <!-- ========== 资金账户 ========== -->
        <section v-show="showAccountPage" class="fin-section">
          <div class="surface filter-card" v-show="showSearch">
            <div class="filter-head">
              <div class="filter-title"><span class="glyph"></span>筛选条件</div>
            </div>
            <div class="filter-bar">
              <div class="field">
                <label>账户编码</label>
                <div class="control">
                  <el-input v-model="accQuery.accountCode" placeholder="请输入账户编码" clearable @keyup.enter="handleAccSearch">
                    <template #prefix><el-icon><Search /></el-icon></template>
                  </el-input>
                </div>
              </div>
              <div class="field">
                <label>账户名称</label>
                <div class="control">
                  <el-input v-model="accQuery.accountName" placeholder="请输入账户名称" clearable @keyup.enter="handleAccSearch">
                    <template #prefix><el-icon><Search /></el-icon></template>
                  </el-input>
                </div>
              </div>
              <div class="field">
                <label>账户类型</label>
                <div class="control is-select">
                  <el-select v-model="accQuery.accountType" placeholder="全部" clearable @change="handleAccSearch">
                    <template #prefix><el-icon><Filter /></el-icon></template>
                    <el-option label="银行账户" value="BANK" />
                    <el-option label="现金" value="CASH" />
                    <el-option label="第三方支付" value="THIRD_PARTY" />
                    <el-option label="承兑汇票" value="NOTE" />
                  </el-select>
                </div>
              </div>
            </div>
            <div class="filter-actions">
              <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ accFilterCount }} 个条件，支持回车快速搜索</div>
              <div class="filter-buttons">
                <el-button icon="RefreshLeft" @click="resetAccQuery">重置</el-button>
                <el-button type="primary" icon="Search" @click="handleAccSearch">搜索</el-button>
              </div>
            </div>
          </div>

          <div class="surface">
            <div class="toolbar">
              <div class="left">
                <el-button v-hasPermi="['fin:fund:add']" type="primary" plain icon="Plus" @click="handleAddAccount">新增账户</el-button>
                <el-button v-hasPermi="['fin:fund:edit']" type="success" icon="Edit" :disabled="singleAcc === null" @click="handleEditAccount">修改</el-button>
                <el-button v-hasPermi="['fin:fund:remove']" type="danger" icon="Delete" :disabled="accSelection.length === 0" @click="handleDeleteAccount">删除</el-button>
              </div>
              <div class="right">
                <right-toolbar v-model:show-search="showSearch" :columns="accColumns" storageKey="fin_fund_account_index" @query-table="getAccountList" />
                <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
              </div>
            </div>

            <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
              <template #title>
                <span class="hint-text">
                  银行账号<b>加密存储</b>，界面仅显示后4 位（****1234）；系统永不外发完整账号。
                  有流水或余额非零的账户不可删除。
                </span>
              </template>
            </el-alert>

          <div class="table-wrap">
            <el-table v-loading="loading" :data="accountList" border
                      :ref="accCol.tableRef" @header-dragend="accCol.onHeaderDragEnd"
                      @selection-change="handleAccSelection" @current-change="handleAccCurrent">
              <el-table-column type="selection" width="50" align="center" />
              <el-table-column type="index" label="序号" width="85" align="center" />
              <el-table-column key="accountCode" prop="accountCode" label="账户编码" :width="accCol.colWidth('accountCode', 130)" resizable align="center" v-if="accColumns.accountCode.visible" />
              <el-table-column key="accountName" prop="accountName" label="账户名称" :min-width="accCol.colWidth('accountName', 180)" resizable align="center" show-overflow-tooltip v-if="accColumns.accountName.visible" />
              <el-table-column key="accountType" label="账户类型" :width="accCol.colWidth('accountType', 120)" resizable align="center" v-if="accColumns.accountType.visible">
                <template #default="{ row }">
                  <span class="badge violet">{{ accTypeLabel(row.accountType) }}</span>
                </template>
              </el-table-column>
              <el-table-column key="bankName" prop="bankName" label="开户行" :min-width="accCol.colWidth('bankName', 160)" resizable align="center" show-overflow-tooltip v-if="accColumns.bankName.visible">
                <template #default="{ row }">{{ row.bankName || '—' }}</template>
              </el-table-column>
              <el-table-column key="accountTail" label="账号" :width="accCol.colWidth('accountTail', 130)" resizable align="center" v-if="accColumns.accountTail.visible">
                <template #default="{ row }">
                  <span class="account-no" v-if="row.accountTail">****{{ row.accountTail }}</span>
                  <span v-else class="text-muted">—</span>
                </template>
              </el-table-column>
              <el-table-column key="openingBalance" label="期初余额" :width="accCol.colWidth('openingBalance', 140)" resizable align="center" v-if="accColumns.openingBalance.visible">
                <template #default="{ row }">{{ formatAmount(row.openingBalance) }}</template>
              </el-table-column>
              <el-table-column key="balance" label="当前余额" :width="accCol.colWidth('balance', 150)" resizable align="center" v-if="accColumns.balance.visible">
                <template #default="{ row }">
                  <span class="amount-cell strong" :class="Number(row.currentBalance) < 0 ? 'text-out' : ''">
                    {{ formatAmount(row.currentBalance) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column key="status" label="状态" :width="accCol.colWidth('status', 90)" resizable align="center" v-if="accColumns.status.visible">
                <template #default="{ row }">
                  <span class="badge" :class="row.status === '0' ? 'green' : 'gray'">
                    <span class="dot"></span>{{ row.status === '0' ? '启用' : '停用' }}
                  </span>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="accQuery.pageNum"
              v-model:page-size="accQuery.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper" />
          </div>
          </div>
        </section>

        <!-- ========== 资金流水 ========== -->
        <section v-show="showFlowPage" class="fin-section">
          <div class="surface filter-card" v-show="showSearch">
            <div class="filter-head">
              <div class="filter-title"><span class="glyph"></span>筛选条件</div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
            </div>
            <div class="filter-bar">
              <div class="field">
                <label>流水单号</label>
                <div class="control">
                  <el-input v-model="flowQuery.flowNo" placeholder="请输入流水单号" clearable @keyup.enter="handleFlowSearch">
                    <template #prefix><el-icon><Search /></el-icon></template>
                  </el-input>
                </div>
              </div>
              <div class="field">
                <label>资金账户</label>
                <div class="control is-select">
                  <el-select v-model="flowQuery.accountId" placeholder="全部账户" clearable @change="handleFlowSearch">
                    <template #prefix><el-icon><Filter /></el-icon></template>
                    <el-option v-for="a in accountList" :key="a.accountId"
                               :label="`${a.accountName}（${formatAmount(a.currentBalance)}）`"
                               :value="a.accountId" />
                  </el-select>
                </div>
              </div>
              <div class="field">
                <label>业务类型</label>
                <div class="control is-select">
                  <el-select v-model="flowQuery.bizType" placeholder="全部" clearable @change="handleFlowSearch">
                    <template #prefix><el-icon><Filter /></el-icon></template>
                    <el-option label="收款" value="IN" />
                    <el-option label="付款" value="OUT" />
                    <el-option label="银行转账" value="TRANSFER" />
                    <el-option label="手续费" value="FEE" />
                    <el-option label="利息收入" value="INTEREST" />
                  </el-select>
                </div>
              </div>
              <div class="field">
                <label>流水日期</label>
                <div class="control">
                  <el-date-picker v-model="flowDateRange" type="daterange" range-separator="-"
                                  start-placeholder="开始日期" end-placeholder="结束日期"
                                  value-format="YYYY-MM-DD" />
                </div>
              </div>
            </div>
            <div class="filter-actions">
              <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ flowFilterCount }} 个条件，支持回车快速搜索</div>
              <div class="filter-buttons">
                <el-button icon="RefreshLeft" @click="resetFlowQuery">重置</el-button>
                <el-button type="primary" icon="Search" @click="handleFlowSearch">搜索</el-button>
              </div>
            </div>
          </div>

          <div class="surface">
            <div class="toolbar">
              <div class="left">
                <el-button v-hasPermi="['fin:fund:flow']" type="warning" icon="Coin" @click="handleAddFlow">登记流水</el-button>
                <el-button v-hasPermi="['fin:fund:flow']" type="primary" icon="Switch" @click="handleTransferClick">账户转账</el-button>
                <el-button v-hasPermi="['fin:fund:reconcile']" type="info" icon="Finished" :disabled="flowSelection.length === 0" @click="handleReconcile">标记已对账</el-button>
                <el-button v-hasPermi="['fin:fund:export']" icon="Download" @click="handleExport">导出流水</el-button>
              </div>
              <div class="right">
                <right-toolbar v-model:show-search="showSearch" :columns="flowColumns" storageKey="fin_fund_flow_index" @query-table="getFlowList" />
                <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
              </div>
            </div>

          <div class="table-wrap">
            <el-table v-loading="flowLoading" :data="flowList" border
                      :ref="flowCol.tableRef" @header-dragend="flowCol.onHeaderDragEnd"
                      @selection-change="handleFlowSelection">
              <el-table-column type="selection" width="50" align="center" :selectable="row => row.reconcileStatus !== '1'" />
              <el-table-column type="index" label="序号" width="85" align="center" />
              <el-table-column key="flowNo" prop="flowNo" label="流水单号" :width="flowCol.colWidth('flowNo', 180)" resizable align="center" v-if="flowColumns.flowNo.visible" />
              <el-table-column key="flowDate" prop="flowDate" label="流水日期" :width="flowCol.colWidth('flowDate', 110)" resizable align="center" v-if="flowColumns.flowDate.visible" />
              <el-table-column key="accountId" prop="accountName" label="资金账户" :min-width="flowCol.colWidth('accountId', 150)" resizable align="center" show-overflow-tooltip v-if="flowColumns.accountId.visible" />
              <el-table-column key="direction" label="收支方向" :width="flowCol.colWidth('direction', 90)" resizable align="center" v-if="flowColumns.direction.visible">
                <template #default="{ row }">
                  <span class="badge" :class="row.direction === 'IN' ? 'green' : 'red'">{{ row.direction === 'IN' ? '收' : '付' }}</span>
                </template>
              </el-table-column>
              <el-table-column key="bizType" label="业务类型" :width="flowCol.colWidth('bizType', 110)" resizable align="center" v-if="flowColumns.bizType.visible">
                <template #default="{ row }">
                  <span class="badge violet">{{ bizTypeLabel(row.bizType) }}</span>
                </template>
              </el-table-column>
              <el-table-column key="amount" label="金额" :width="flowCol.colWidth('amount', 140)" resizable align="center" v-if="flowColumns.amount.visible">
                <template #default="{ row }">
                  <span class="amount-cell strong" :class="row.direction === 'IN' ? 'text-in' : 'text-out'">
                    {{ row.direction === 'IN' ? '+' : '-' }}{{ formatAmount(row.amount) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column key="balanceAfter" label="交易后余额" :width="flowCol.colWidth('balanceAfter', 150)" resizable align="center" v-if="flowColumns.balanceAfter.visible">
                <template #default="{ row }">
                  <span class="amount-cell">{{ formatAmount(row.balanceAfter) }}</span>
                </template>
              </el-table-column>
              <el-table-column key="counterparty" prop="counterparty" label="对方单位" :min-width="flowCol.colWidth('counterparty', 140)" resizable align="center" show-overflow-tooltip v-if="flowColumns.counterparty.visible">
                <template #default="{ row }">{{ row.counterparty || '—' }}</template>
              </el-table-column>
              <el-table-column key="summary" label="流水摘要" :min-width="flowCol.colWidth('summary', 150)" resizable align="center" show-overflow-tooltip v-if="flowColumns.summary.visible">
                <template #default="{ row }">{{ row.sourceKey || row.remark || '—' }}</template>
              </el-table-column>
              <el-table-column key="reconcileStatus" label="对账状态" :width="flowCol.colWidth('reconcileStatus', 100)" resizable align="center" v-if="flowColumns.reconcileStatus.visible">
                <template #default="{ row }">
                  <span class="badge" :class="row.reconcileStatus === '1' ? 'green' : 'amber'">
                    <span class="dot"></span>{{ row.reconcileStatus === '1' ? '已对账' : '未对账' }}
                  </span>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="flowQuery.pageNum"
              v-model:page-size="flowQuery.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="flowTotal"
              layout="total, sizes, prev, pager, next, jumper" />
          </div>
          </div>
        </section>
<!-- ========== 银行对账 ========== -->
        <section v-show="showRecon" class="fin-section">
          <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 12px">
            <template #title>
              <span class="hint-text">
                对账流程：<b>导入银行流水 → 自动匹配 → 余额核对</b>。
                自动匹配要求<b>金额严格相等 + 日期 ±3 天 + 同账户</b>；
                未匹配项即为<b>未达账项</b>，需逐笔核实。
              </span>
            </template>
          </el-alert>

          <div class="toolbar">
            <div class="left">
              <el-button icon="Search" @click="getStatements">刷新</el-button>
              <el-button v-hasPermi="['fin:fund:reconcile']" type="primary" plain icon="Upload"
                         @click="handleImport">导入对账单</el-button>
            </div>
            <div class="right">
              <right-toolbar :columns="reconColumns" storageKey="fin_fund_recon_index" @query-table="getStatements" />
            </div>
          </div>

          <div class="table-wrap">
            <el-table v-loading="stLoading" :data="statements" border
                      :ref="reconCol.tableRef" @header-dragend="reconCol.onHeaderDragEnd">
              <el-table-column type="index" label="序号" width="85" align="center" />
              <el-table-column key="statementDate" prop="statementDate" label="对账日期" :width="reconCol.colWidth('statementDate', 120)" resizable align="center" v-if="reconColumns.statementDate.visible" />
              <el-table-column key="accountId" prop="accountName" label="账户名称" :min-width="reconCol.colWidth('accountId', 180)" resizable align="center" show-overflow-tooltip v-if="reconColumns.accountId.visible" />
              <el-table-column key="bankBeginBalance" label="银行期初余额" :width="reconCol.colWidth('bankBeginBalance', 140)" resizable align="center" v-if="reconColumns.bankBeginBalance.visible">
                <template #default="{ row }">
                  <span class="amount-cell">{{ fmt(row.bankBeginBalance) }}</span>
                </template>
              </el-table-column>
              <el-table-column key="bankEndBalance" label="银行期末余额" :width="reconCol.colWidth('bankEndBalance', 140)" resizable align="center" v-if="reconColumns.bankEndBalance.visible">
                <template #default="{ row }">
                  <span class="amount-cell">{{ fmt(row.bankEndBalance) }}</span>
                </template>
              </el-table-column>
              <el-table-column key="bookEndBalance" label="账面期末余额" :width="reconCol.colWidth('bookEndBalance', 140)" resizable align="center" v-if="reconColumns.bookEndBalance.visible">
                <template #default="{ row }">
                  <span class="amount-cell">{{ fmt(row.bookEndBalance) }}</span>
                </template>
              </el-table-column>
              <el-table-column key="diffAmount" label="差异" :width="reconCol.colWidth('diffAmount', 130)" resizable align="center" v-if="reconColumns.diffAmount.visible">
                <template #default="{ row }">
                  <span class="amount-cell strong"
                        :class="Number(row.diffAmount) === 0 ? 'text-success' : 'text-danger'">
                    {{ fmt(row.diffAmount) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column key="status" label="状态" :width="reconCol.colWidth('status', 100)" resizable align="center" v-if="reconColumns.status.visible">
                <template #default="{ row }">
                  <span class="badge" :class="row.status === 'RECONCILED' ? 'green' : (row.status === 'DIFF' ? 'red' : 'blue')">
                    <span class="dot"></span>{{ statusLabel(row.status) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
                <template #default="{ row }">
                  <div class="action-btn-row">
                    <el-button link type="primary" icon="View" @click="showDetail(row)">明细</el-button>
                    <el-button v-hasPermi="['fin:fund:reconcile']" link type="primary"
                               icon="MagicStick" @click="handleAutoMatch(row)">自动匹配</el-button>
                    <el-button v-hasPermi="['fin:fund:reconcile']" link type="success"
                               icon="Sort" @click="handleBankReconcile(row)">余额核对</el-button>
                    <el-button link type="warning" icon="Warning" @click="handleUnreconciled(row)">未达账项</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>

    <!-- 账户编辑弹框 -->
    <el-dialog v-model="showAccount" class="rd-dialog" width="640px" append-to-body>
      <template #header>
        <div class="rd-detail-header"><span class="rd-detail-title">{{ accountForm.accountId ? '修改资金账户' : '新增资金账户' }}</span></div>
      </template>
      <el-form ref="accountFormRef" :model="accountForm" :rules="accountRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="账户编码" prop="accountCode">
              <el-input v-model="accountForm.accountCode" :disabled="!!accountForm.accountId" placeholder="如 BANK001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账户类型" prop="accountType">
              <el-select v-model="accountForm.accountType" placeholder="请选择" style="width: 100%" @change="onTypeChange">
                <el-option label="银行账户" value="BANK" />
                <el-option label="现金" value="CASH" />
                <el-option label="第三方支付" value="THIRD_PARTY" />
                <el-option label="银行承兑汇票" value="NOTE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="账户名称" prop="accountName">
          <el-input v-model="accountForm.accountName" placeholder="如 基本存款账户" />
        </el-form-item>
        <template v-if="needAccountNo">
          <el-form-item label="开户行" prop="bankName">
            <el-input v-model="accountForm.bankName" placeholder="如 中国工商银行武汉分行" />
          </el-form-item>
          <el-form-item :label="accountForm.accountId ? '新账号' : '银行账号'" :prop="accountForm.accountId ? '' : 'accountTail'">
            <el-input v-model="accountForm.accountTail" placeholder="6-32 位数字，系统加密存储" />
            <div class="form-tip">账号加密入库，保存后仅显示后 4 位</div>
          </el-form-item>
        </template>
        <el-form-item label="期初余额" prop="openingBalance">
          <el-input v-model="accountForm.openingBalance" type="number" placeholder="0.00" style="text-align: right" />
          <div class="form-tip">新账户的当前余额将初始化为期初余额</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="accountForm.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="accountForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAccount">确 定</el-button>
          <el-button @click="showAccount = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 流水登记弹框 -->
    <el-dialog v-model="showFlow" class="rd-dialog" width="560px" append-to-body>
      <template #header>
        <div class="rd-detail-header"><span class="rd-detail-title">登记资金流水</span></div>
      </template>
      <el-form ref="flowFormRef" :model="flowForm" :rules="flowRules" label-width="90px">
        <el-form-item label="资金账户" prop="accountId">
          <el-select v-model="flowForm.accountId" placeholder="请选择" style="width: 100%">
            <el-option v-for="a in accountList" :key="a.accountId"
                       :label="`${a.accountName}（余额 ${formatAmount(a.currentBalance)}）`"
                       :value="a.accountId" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="收支方向" prop="direction">
              <el-radio-group v-model="flowForm.direction">
                <el-radio-button value="IN">收入</el-radio-button>
                <el-radio-button value="OUT">支出</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务类型" prop="bizType">
              <el-select v-model="flowForm.bizType" placeholder="请选择" style="width: 100%">
                <el-option label="收款" value="IN" />
                <el-option label="付款" value="OUT" />
                <el-option label="手续费" value="FEE" />
                <el-option label="利息收入" value="INTEREST" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="金额" prop="amount">
              <el-input v-model="flowForm.amount" type="number" placeholder="0.00" style="text-align: right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="流水日期" prop="flowDate">
              <el-date-picker v-model="flowForm.flowDate" type="date" value-format="YYYY-MM-DD"
                              style="width: 100%" placeholder="默认今日" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="摘要">
          <el-input v-model="flowForm.sourceKey" placeholder="如 货款结算 / 运费" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="flowForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFlow">确 定</el-button>
          <el-button @click="showFlow = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 转账弹框 -->
    <el-dialog v-model="showTransfer" class="rd-dialog" width="520px" append-to-body>
      <template #header>
        <div class="rd-detail-header"><span class="rd-detail-title">账户间转账</span></div>
      </template>
      <el-form ref="transferFormRef" :model="transferForm" :rules="transferRules" label-width="90px">
        <el-form-item label="转出账户" prop="fromAccountId">
          <el-select v-model="transferForm.fromAccountId" placeholder="请选择" style="width: 100%">
            <el-option v-for="a in accountList" :key="a.accountId"
                       :label="`${a.accountName}（余额 ${formatAmount(a.currentBalance)}）`"
                       :value="a.accountId" />
          </el-select>
        </el-form-item>
        <el-form-item label="转入账户" prop="toAccountId">
          <el-select v-model="transferForm.toAccountId" placeholder="请选择" style="width: 100%">
            <el-option v-for="a in accountList" :key="a.accountId"
                       :label="`${a.accountName}（余额 ${formatAmount(a.currentBalance)}）`"
                       :value="a.accountId" />
          </el-select>
        </el-form-item>
        <el-form-item label="转账金额" prop="amount">
          <el-input v-model="transferForm.amount" type="number" placeholder="0.00" style="text-align: right" />
        </el-form-item>
        <el-form-item label="转账日期">
          <el-date-picker v-model="transferForm.transferDate" type="date" value-format="YYYY-MM-DD"
                          style="width: 100%" placeholder="默认今日" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="transferForm.remark" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitTransfer">确 定</el-button>
          <el-button @click="showTransfer = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 导入对账单 -->
    <el-dialog v-model="showImport" class="rd-dialog" width="760px" append-to-body>
        <template #header>
          <div class="rd-detail-header"><span class="rd-detail-title">导入银行对账单</span></div>
        </template>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>
          <span class="hint-text">
            手工录入或粘贴银行流水。<b>金额规则：收入为正、支出为负</b>
            （系统据此汇总期末余额）。
          </span>
        </template>
      </el-alert>
      <el-form label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="资金账户" required>
              <el-select v-model="impForm.accountId" placeholder="请选择" style="width: 100%">
                <el-option v-for="a in accountList" :key="a.accountId"
                           :label="a.accountName" :value="a.accountId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="对账日期" required>
              <el-date-picker v-model="impForm.statementDate" type="date" value-format="YYYY-MM-DD"
                              style="width: 100%" placeholder="默认今日" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="银行期初余额" required>
          <el-input v-model="impForm.bankBeginBalance" placeholder="如 1000000.00" />
        </el-form-item>
      </el-form>

      <el-divider content-position="left"><span class="divider-text">银行流水明细</span></el-divider>
      <el-table :data="impForm.items" size="small" border>
        <el-table-column label="日期" width="140" align="center">
          <template #default="{ row }">
            <el-date-picker v-model="row.transDate" type="date" value-format="YYYY-MM-DD"
                            style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">
            <el-select v-model="row.transType" style="width: 100%">
              <el-option label="收入" value="收入" />
              <el-option label="支出" value="支出" />
              <el-option label="转账" value="转账" />
              <el-option label="手续费" value="手续费" />
              <el-option label="利息" value="利息" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="金额（收+/支-）" width="140" align="right">
          <template #default="{ row }">
            <el-input v-model="row.transAmount" placeholder="-2000.00"
                      :class="Number(row.transAmount) < 0 ? 'text-out' : 'text-in'" />
          </template>
        </el-table-column>
        <el-table-column label="对方单位" min-width="150" align="left">
          <template #default="{ row }">
            <el-input v-model="row.counterparty" />
          </template>
        </el-table-column>
        <el-table-column label="摘要" min-width="140" align="left">
          <template #default="{ row }">
            <el-input v-model="row.summary" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="70" align="center">
          <template #default="{ $index }">
            <el-button link type="danger" icon="Delete"
                       @click="impForm.items.splice($index, 1)" />
          </template>
        </el-table-column>
      </el-table>
      <div class="item-actions">
        <el-button icon="Plus" size="small" @click="addImpItem">添加一行</el-button>
        <span class="form-tip">共 {{ impForm.items.length }} 行，合计
          <b :class="impTotal < 0 ? 'text-out' : 'text-in'">{{ fmt(impTotal) }}</b>
        </span>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitImport">确认导入</el-button>
          <el-button @click="showImport = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 对账明细 -->
    <el-dialog v-model="showDetailBox" class="rd-dialog" width="1000px" append-to-body>
        <template #header>
          <div class="rd-detail-header"><span class="rd-detail-title">对账明细</span></div>
        </template>
      <div v-if="detail.stat" class="match-stat-bar">
        <span>共 {{ detail.stat.total }} 笔</span>
        <span class="text-success">已匹配 {{ detail.stat.matched }}</span>
        <span class="text-danger">未匹配 {{ detail.stat.unmatched }}</span>
        <span>未匹配金额
          <b :class="Number(detail.stat.unmatchedAmount) === 0 ? 'text-success' : 'text-danger'">
            {{ fmt(detail.stat.unmatchedAmount) }}
          </b>
        </span>
      </div>
      <el-table :data="detail.items || []" size="small" border max-height="420">
        <el-table-column prop="transDate" label="日期" width="110" align="center" />
        <el-table-column prop="transType" label="类型" width="90" align="center" />
        <el-table-column label="金额" width="130" align="right">
          <template #default="{ row }">
            <span class="amount-cell" :class="Number(row.transAmount) < 0 ? 'text-out' : 'text-in'">
              {{ fmt(row.transAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="counterparty" label="对方单位" min-width="150" align="left" />
        <el-table-column prop="summary" label="摘要" min-width="150" align="left" />
        <el-table-column label="匹配状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.matchStatus === '1' ? 'success' : 'warning'" effect="plain" size="small">
              {{ row.matchStatus === '1' ? '已匹配' : '未匹配' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" align="center">
          <template #default="{ row }">
            <el-button v-if="row.matchStatus !== '1'" v-hasPermi="['fin:fund:reconcile']"
                       link type="primary" icon="Link" @click="handleManualMatch(row)">手动匹配</el-button>
            <el-button v-else v-hasPermi="['fin:fund:reconcile']"
                       link type="warning" icon="RefreshLeft" @click="handleUnmatch(row)">解除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showDetailBox = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 候选匹配 -->
    <el-dialog v-model="showCandidate" class="rd-dialog" width="760px" append-to-body>
        <template #header>
          <div class="rd-detail-header"><span class="rd-detail-title">选择要匹配的账面流水</span></div>
        </template>
      <el-alert v-if="candidates.length === 0" type="warning" :closable="false" show-icon
                title="未找到金额与日期相近的账面流水，请检查是否漏记账或金额有误" />
      <el-table v-else :data="candidates" size="small" border max-height="380">
        <el-table-column prop="flowNo" label="流水号" width="130" align="left" />
        <el-table-column prop="flowDate" label="日期" width="110" align="center" />
        <el-table-column label="方向" width="80" align="center">
          <template #default="{ row }">
            <span class="dir-badge" :class="row.direction === 'IN' ? 'is-debit' : 'is-credit'">
              {{ row.direction === 'IN' ? '收' : '付' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="130" align="right">
          <template #default="{ row }">
            <span class="amount-cell">{{ fmt(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bizType" label="业务类型" width="110" align="center" />
        <el-table-column prop="remark" label="摘要" min-width="150" align="left" />
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button link type="primary" icon="Check" @click="confirmMatch(row)">选定</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showCandidate = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 余额核对结果 -->
    <el-dialog v-model="showResult" class="rd-dialog" width="560px" append-to-body>
        <template #header>
          <div class="rd-detail-header"><span class="rd-detail-title">余额核对结果</span></div>
        </template>
      <div class="recon-result" :class="recon.balanced ? 'is-balanced' : 'is-unbalanced'">
        <div class="rr-row"><span>银行期初</span><b>{{ fmt(recon.bankBegin) }}</b></div>
        <div class="rr-row"><span>银行本期发生</span><b>{{ fmt(recon.bankTotal) }}</b></div>
        <div class="rr-row"><span>银行期末</span><b>{{ fmt(recon.bankEnd) }}</b></div>
        <div class="rr-divider" />
        <div class="rr-row"><span>账面期初</span><b>{{ fmt(recon.bookBegin) }}</b></div>
        <div class="rr-row"><span>账面本期净发生</span><b>{{ fmt(recon.bookNet) }}</b></div>
        <div class="rr-row"><span>账面期末</span><b>{{ fmt(recon.bookEnd) }}</b></div>
        <div class="rr-divider" />
        <div class="rr-row is-diff">
          <span>差异</span>
          <b>{{ fmt(recon.diff) }}</b>
        </div>
      </div>
      <el-alert :type="recon.balanced ? 'success' : 'warning'" :closable="false" show-icon
                :title="recon.balanced ? '账实相符，对账完成' : '存在差异，请查看未达账项'"
                style="margin-top: 12px" />
      <template #footer>
        <el-button @click="showResult = false">关 闭</el-button>
        <el-button v-if="!recon.balanced" type="warning" @click="showResult = false; handleUnreconciled(currentStatement)">
          查看未达账项
        </el-button>
      </template>
    </el-dialog>

    <!-- 未达账项 -->
    <el-dialog v-model="showUnrec" class="rd-dialog" width="900px" append-to-body>
        <template #header>
          <div class="rd-detail-header"><span class="rd-detail-title">未达账项</span></div>
        </template>
      <el-tabs v-model="unrecTab">
        <el-tab-pane :label="`银行有、账面无（${(unrec.bankOnly || []).length}）`">
          <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 10px"
                    title="银行已记账但我方账面未记录，可能原因：手续费、代扣款、未及时入账的收款" />
          <el-table :data="unrec.bankOnly || []" size="small" border max-height="340">
            <el-table-column prop="transDate" label="日期" width="110" align="center" />
            <el-table-column prop="transType" label="类型" width="90" align="center" />
            <el-table-column label="金额" width="130" align="right">
              <template #default="{ row }">
                <span class="amount-cell" :class="Number(row.transAmount) < 0 ? 'text-out' : 'text-in'">
                  {{ fmt(row.transAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="counterparty" label="对方单位" min-width="160" align="left" />
            <el-table-column prop="summary" label="摘要" min-width="160" align="left" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane :label="`账面有、银行无（${(unrec.bookOnly || []).length}）`">
          <el-alert type="info" :closable="false" show-icon style="margin-bottom: 10px"
                    title="我方账面已记账但银行未清算，可能原因：在途转账、支票未兑现、记账错误" />
          <el-table :data="unrec.bookOnly || []" size="small" border max-height="340">
            <el-table-column prop="flowNo" label="流水号" width="130" align="left" />
            <el-table-column prop="flowDate" label="日期" width="110" align="center" />
            <el-table-column label="方向" width="80" align="center">
              <template #default="{ row }">
                <span class="dir-badge" :class="row.direction === 'IN' ? 'is-debit' : 'is-credit'">
                  {{ row.direction === 'IN' ? '收' : '付' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="金额" width="130" align="right">
              <template #default="{ row }">
                <span class="amount-cell">{{ fmt(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="bizType" label="业务类型" width="110" align="center" />
            <el-table-column prop="remark" label="摘要" min-width="150" align="left" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="showUnrec = false">关 闭</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="showStatusHelp" title="资金管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content"><h4>一、状态流转图</h4><div class="status-flow"><el-tag type="primary">账户维护</el-tag><span class="flow-arrow">→</span><el-tag type="warning">流水登记</el-tag><span class="flow-arrow">→</span><el-tag type="success">银行对账</el-tag></div><h4>二、各状态说明</h4><el-descriptions :column="1" border><el-descriptions-item label="资金账户">维护银行、现金等资金账户的基础信息与期初余额</el-descriptions-item><el-descriptions-item label="资金流水">登记各账户的收入/支出流水，系统自动更新账户余额</el-descriptions-item><el-descriptions-item label="银行对账">比对银行流水与账面记录，确认未达账项</el-descriptions-item></el-descriptions><h4>三、重点业务规则</h4><el-row :gutter="16"><el-col :span="12"><div class="highlight-card highlight-primary"><div class="highlight-card-title">账户余额联动</div><div class="highlight-card-body">资金流水登记后<strong>自动更新对应账户余额</strong>，无需手工调整</div></div></el-col><el-col :span="12"><div class="highlight-card highlight-warning"><div class="highlight-card-title">流水完整可溯</div><div class="highlight-card-body">每笔流水记录账户、方向、金额与关联单据，<strong>资金来源去向全程可追溯</strong></div></div></el-col></el-row><el-row :gutter="16" style="margin-top: 12px;"><el-col :span="12"><div class="highlight-card highlight-success"><div class="highlight-card-title">银行对账闭环</div><div class="highlight-card-body">银行对账页签比对银行流水与账面流水，<strong>未达账项逐笔确认</strong></div></div></el-col><el-col :span="12"><div class="highlight-card highlight-danger"><div class="highlight-card-title">权限控制</div><div class="highlight-card-body">资金账户与流水的增删改受权限控制，<strong>请按角色分配操作权限</strong></div></div></el-col></el-row><h4>四、业务操作流程</h4><el-timeline><el-timeline-item type="primary" :hollow="true"><strong>账户维护：</strong>在资金账户页签维护账户信息与期初余额</el-timeline-item><el-timeline-item type="warning" :hollow="true"><strong>流水登记：</strong>在资金流水页签登记收入/支出流水，余额自动更新</el-timeline-item><el-timeline-item type="success" :hollow="true"><strong>银行对账：</strong>在银行对账页签比对银行流水，确认未达账项</el-timeline-item><el-timeline-item type="info" :hollow="true"><strong>余额监控：</strong>通过账户列表实时监控各账户资金余额</el-timeline-item></el-timeline></div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="FinFund">
const showStatusHelp = ref(false)
import {
  listStatement, getStatement, importStatement, autoMatch,
  manualMatch as manualMatchApi, unmatch, matchCandidates,
  reconcileStatement, unreconciled, delStatement
} from '@/api/fin/configExt'
import {
  listAccount, addAccount, updateAccount, delAccount,
  listFlow, addFlow, transferFund, reconcileFlow
} from '@/api/fin/fund'
import { useColumnResize } from "@/composables/useColumnResize"

const { proxy } = getCurrentInstance()

/** 页面分区由路由包装组件通过 prop 传入（三个独立 SFC 路由组件，避免同一组件多路由的过渡异常） */
const props = defineProps({
  page: { type: String, default: 'account' }
})
const showAccountPage = computed(() => props.page === 'account')
const showFlowPage = computed(() => props.page === 'flow')
const showRecon = computed(() => props.page === 'reconcile')
const loading = ref(false)
const flowLoading = ref(false)
const accountList = ref([])
const flowList = ref([])
const total = ref(0)
const flowTotal = ref(0)
const accSelection = ref([])
const flowSelection = ref([])
const singleAcc = ref(null)
const flowDateRange = ref([])

// ==================== 列宽持久化与列显隐 ====================
const accCol = useColumnResize('fin_fund_account_index')
const flowCol = useColumnResize('fin_fund_flow_index')
const reconCol = useColumnResize('fin_fund_recon_index')

const accColumns = reactive({
  accountCode: { key: 'accountCode', label: '账户编码', visible: true },
  accountName: { key: 'accountName', label: '账户名称', visible: true },
  accountType: { key: 'accountType', label: '账户类型', visible: true },
  accountTail: { key: 'accountTail', label: '账号', visible: true },
  bankName: { key: 'bankName', label: '开户行', visible: true },
  openingBalance: { key: 'openingBalance', label: '期初余额', visible: true },
  balance: { key: 'balance', label: '当前余额', visible: true },
  status: { key: 'status', label: '状态', visible: true }
})
const flowColumns = reactive({
  flowNo: { key: 'flowNo', label: '流水单号', visible: true },
  accountId: { key: 'accountId', label: '资金账户', visible: true },
  flowDate: { key: 'flowDate', label: '流水日期', visible: true },
  bizType: { key: 'bizType', label: '业务类型', visible: true },
  direction: { key: 'direction', label: '收支方向', visible: true },
  amount: { key: 'amount', label: '金额', visible: true },
  counterparty: { key: 'counterparty', label: '对方单位', visible: true },
  balanceAfter: { key: 'balanceAfter', label: '交易后余额', visible: true },
  reconcileStatus: { key: 'reconcileStatus', label: '对账状态', visible: true },
  summary: { key: 'summary', label: '流水摘要', visible: true }
})
const reconColumns = reactive({
  accountId: { key: 'accountId', label: '资金账户', visible: true },
  statementDate: { key: 'statementDate', label: '对账日期', visible: true },
  bankBeginBalance: { key: 'bankBeginBalance', label: '银行期初余额', visible: true },
  bankEndBalance: { key: 'bankEndBalance', label: '银行期末余额', visible: true },
  bookEndBalance: { key: 'bookEndBalance', label: '账面期末余额', visible: true },
  diffAmount: { key: 'diffAmount', label: '差异', visible: true },
  status: { key: 'status', label: '状态', visible: true }
})

/** 列显隐默认全开；宽度由 useColumnResize 持久化（localStorage + sys_table_config） */
function loadColumnVisibility() {}

const showSearch = ref(true)

/** 账户筛选卡片已启用条件数 */
const accFilterCount = computed(() => {
  let n = 0
  if (accQuery.value.accountCode) n++
  if (accQuery.value.accountName) n++
  if (accQuery.value.accountType) n++
  return n
})

/** 流水筛选卡片已启用条件数 */
const flowFilterCount = computed(() => {
  let n = 0
  if (flowQuery.value.flowNo) n++
  if (flowQuery.value.accountId) n++
  if (flowQuery.value.bizType) n++
  if (flowDateRange.value && flowDateRange.value.length === 2) n++
  return n
})

const showAccount = ref(false)
const showFlow = ref(false)
const showTransfer = ref(false)

const accQuery = ref({ pageNum: 1, pageSize: 20, accountCode: undefined, accountName: undefined, accountType: undefined })
const flowQuery = ref({ pageNum: 1, pageSize: 20, accountId: undefined, direction: undefined, bizType: undefined, flowNo: undefined })

function resetAccQuery() {
  accQuery.value = { pageNum: 1, pageSize: 20, accountCode: undefined, accountName: undefined, accountType: undefined }
  getAccountList()
}

function handleAccSearch() {
  getAccountList()
}

function handleFlowSearch() {
  getFlowList()
}

/** 流水页「账户转账」入口：先校验账户数 */
function handleTransferClick() {
  if (accountList.value.length < 2) {
    proxy.$modal.msgWarning('至少需要两个资金账户才能转账')
    return
  }
  Object.assign(transferForm, {
    fromAccountId: undefined, toAccountId: undefined, amount: undefined,
    transferDate: new Date().toISOString().slice(0, 10), remark: ''
  })
  showTransfer.value = true
}

const accountForm = reactive({})
const accountRules = {
  accountCode: [{ required: true, message: '账户编码不能为空', trigger: 'blur' }],
  accountName: [{ required: true, message: '账户名称不能为空', trigger: 'blur' }],
  accountType: [{ required: true, message: '账户类型不能为空', trigger: 'change' }],
  openingBalance: [{ required: true, message: '期初余额不能为空', trigger: 'blur' }]
}

const flowForm = reactive({})
const flowRules = {
  accountId: [{ required: true, message: '请选择资金账户', trigger: 'change' }],
  direction: [{ required: true, message: '请选择收支方向', trigger: 'change' }],
  bizType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  amount: [{ required: true, message: '金额不能为空', trigger: 'blur' }]
}

const transferForm = reactive({})
const transferRules = {
  fromAccountId: [{ required: true, message: '请选择转出账户', trigger: 'change' }],
  toAccountId: [{ required: true, message: '请选择转入账户', trigger: 'change' }],
  amount: [{ required: true, message: '转账金额不能为空', trigger: 'blur' }]
}

/** 银行类账户才需要账号字段 */
const needAccountNo = computed(() => ['BANK', 'NOTE'].includes(accountForm.accountType))

function formatAmount(v) {
  return Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function accTypeLabel(t) {
  return { BANK: '银行账户', CASH: '现金', THIRD_PARTY: '第三方支付', NOTE: '承兑汇票' }[t] || t
}

function accTypeTag(t) {
  return { BANK: 'primary', CASH: 'success', THIRD_PARTY: 'warning', NOTE: 'info' }[t] || 'info'
}

function bizTypeLabel(t) {
  return { IN: '收款', OUT: '付款', TRANSFER: '银行转账', FEE: '手续费', INTEREST: '利息收入' }[t] || t
}

// ==================== 银行对账 ====================

const stLoading = ref(false)
const statements = ref([])
const showImport = ref(false)
const showDetailBox = ref(false)
const showCandidate = ref(false)
const showResult = ref(false)
const showUnrec = ref(false)
const unrecTab = ref('bankOnly')
const detail = ref({})
const candidates = ref([])
const unrec = ref({})
const recon = ref({})
const currentStatement = ref(null)
const currentItem = ref(null)
const impForm = reactive({ accountId: null, statementDate: '', bankBeginBalance: '', items: [] })

/** 导入明细金额合计（收入为正、支出为负） */
const impTotal = computed(() => impForm.items.reduce((s, i) => s + (Number(i.transAmount) || 0), 0))

function fmt(v) {
  return Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function statusLabel(st) {
  return { IMPORTED: '已导入', RECONCILED: '已对平', DIFF: '有差异' }[st] || st
}

function statusTag(st) {
  return { IMPORTED: 'info', RECONCILED: 'success', DIFF: 'danger' }[st] || 'info'
}

function getStatements() {
  stLoading.value = true
  listStatement({ pageNum: 1, pageSize: 100 }).then(res => {
    statements.value = res.rows || []
    stLoading.value = false
    reconCol.applySavedWidths()
  })
}

function addImpItem() {
  impForm.items.push({
    transDate: impForm.statementDate || new Date().toISOString().slice(0, 10),
    transType: '支出', transAmount: '', counterparty: '', summary: ''
  })
}

function handleImport() {
  Object.assign(impForm, {
    accountId: accountList.value.length ? accountList.value[0].accountId : null,
    statementDate: new Date().toISOString().slice(0, 10),
    bankBeginBalance: '', items: []
  })
  addImpItem()
  showImport.value = true
}

function submitImport() {
  if (!impForm.accountId) {
    proxy.$modal.msgError('请选择资金账户')
    return
  }
  if (!impForm.bankBeginBalance) {
    proxy.$modal.msgError('请填写银行期初余额')
    return
  }
  const items = impForm.items.filter(i => i.transDate && i.transAmount !== '' && i.transAmount != null)
  if (!items.length) {
    proxy.$modal.msgError('请至少填写一条有效的银行流水')
    return
  }
  importStatement({
    accountId: impForm.accountId,
    statementDate: impForm.statementDate,
    bankBeginBalance: Number(impForm.bankBeginBalance),
    items: items.map(i => ({
      transDate: i.transDate,
      transType: i.transType,
      transAmount: Number(i.transAmount),
      counterparty: i.counterparty,
      summary: i.summary
    }))
  }).then(() => {
    proxy.$modal.msgSuccess(`导入成功，共 ${items.length} 条银行流水`)
    showImport.value = false
    getStatements()
  })
}

function showDetail(row) {
  currentStatement.value = row
  getStatement(row.statementId).then(res => {
    detail.value = res.data || {}
    showDetailBox.value = true
  })
}

function handleAutoMatch(row) {
  proxy.$modal.confirm(
    '将按「金额严格相等 + 日期 ±3 天 + 同账户」自动匹配账面流水，是否继续？'
  ).then(() => {
    return autoMatch(row.statementId)
  }).then(res => {
    const d = res.data || {}
    proxy.$modal.msgSuccess(
      `匹配完成：本次新增 ${d.newMatched || 0} 笔，累计已匹配 ${d.matched || 0}/${d.total || 0} 笔`
    )
    if (d.unmatched > 0) {
      proxy.$modal.msgWarning(`仍有 ${d.unmatched} 笔未匹配（未达账项），请核实`)
    }
    getStatements()
  }).catch(() => {})
}

function handleManualMatch(item) {
  currentItem.value = item
  matchCandidates(currentStatement.value.accountId, item.transAmount, item.transDate).then(res => {
    candidates.value = res.data || []
    showCandidate.value = true
  })
}

function confirmMatch(row) {
  manualMatchApi(currentItem.value.itemId, row.flowId).then(() => {
    proxy.$modal.msgSuccess('匹配成功')
    showCandidate.value = false
    showDetail(currentStatement.value)
    getStatements()
  })
}

function handleUnmatch(item) {
  unmatch(item.itemId).then(() => {
    proxy.$modal.msgSuccess('已解除匹配')
    showDetail(currentStatement.value)
  })
}

function handleBankReconcile(row) {
  reconcileStatement(row.statementId).then(res => {
    recon.value = res.data || {}
    showResult.value = true
    getStatements()
  })
}

function handleUnreconciled(row) {
  unreconciled(row.statementId).then(res => {
    unrec.value = res.data || {}
    unrecTab.value = 'bankOnly'
    showUnrec.value = true
  })
}

function getAccountList() {
  loading.value = true
  listAccount(accQuery.value).then(res => {
    accountList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    accCol.applySavedWidths()
  })
}

function getFlowList() {
  flowLoading.value = true
  const params = { ...flowQuery.value }
  if (flowDateRange.value && flowDateRange.value.length === 2) {
    params.params = { beginFlowDate: flowDateRange.value[0], endFlowDate: flowDateRange.value[1] }
  }
  listFlow(params).then(res => {
    flowList.value = res.rows || []
    flowTotal.value = res.total || 0
    flowLoading.value = false
    flowCol.applySavedWidths()
  })
}

function resetFlowQuery() {
  flowQuery.value = { pageNum: 1, pageSize: 20, accountId: undefined, direction: undefined, bizType: undefined, flowNo: undefined }
  flowDateRange.value = []
  getFlowList()
}

function handleAccSelection(rows) { accSelection.value = rows }
function handleAccCurrent(row) { singleAcc.value = row }
function handleFlowSelection(rows) { flowSelection.value = rows }

function onTypeChange() {
  // 切换到现金类时清空已填账号，避免误提交
  if (!needAccountNo.value) {
    accountForm.accountTail = undefined
    accountForm.bankName = undefined
  }
}

function handleAddAccount() {
  Object.assign(accountForm, {
    accountId: undefined, accountCode: '', accountName: '', accountType: 'BANK',
    bankName: '', accountTail: '', openingBalance: 0, status: '0', remark: ''
  })
  showAccount.value = true
}

function handleEditAccount() {
  if (!singleAcc.value) {
    proxy.$modal.msgWarning('请先选择要修改的账户')
    return
  }
  Object.assign(accountForm, singleAcc.value, { accountTail: '' })
  showAccount.value = true
}

function submitAccount() {
  proxy.$refs['accountFormRef'].validate(valid => {
    if (!valid) return
    const req = accountForm.accountId ? updateAccount : addAccount
    req({ ...accountForm, openingBalance: Number(accountForm.openingBalance) }).then(() => {
      proxy.$modal.msgSuccess(accountForm.accountId ? '修改成功' : '新增成功')
      showAccount.value = false
      getAccountList()
    })
  })
}

function handleDeleteAccount() {
  const ids = accSelection.value.map(a => a.accountId)
  proxy.$modal.confirm(`是否确认删除选中的 ${ids.length} 个账户？有流水或余额非零的账户将被拒绝。`).then(() => {
    return delAccount(ids)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getAccountList()
  }).catch(() => {})
}

function handleAddFlow() {
  if (accountList.value.length === 0) {
    proxy.$modal.msgWarning('请先新增资金账户')
    return
  }
  Object.assign(flowForm, {
    accountId: undefined, direction: 'IN', bizType: 'IN',
    amount: undefined, flowDate: new Date().toISOString().slice(0, 10),
    sourceKey: '', remark: ''
  })
  showFlow.value = true
}

function submitFlow() {
  proxy.$refs['flowFormRef'].validate(valid => {
    if (!valid) return
    addFlow({ ...flowForm, amount: Number(flowForm.amount) }).then(() => {
      proxy.$modal.msgSuccess('流水登记成功')
      showFlow.value = false
      getFlowList(); getAccountList()
    })
  })
}

function submitTransfer() {
  proxy.$refs['transferFormRef'].validate(valid => {
    if (!valid) return
    if (transferForm.fromAccountId === transferForm.toAccountId) {
      proxy.$modal.msgError('转出与转入账户不能相同')
      return
    }
    transferFund({
      fromAccountId: transferForm.fromAccountId,
      toAccountId: transferForm.toAccountId,
      amount: Number(transferForm.amount),
      remark: transferForm.remark,
      transferDate: transferForm.transferDate
    }).then(res => {
      proxy.$modal.msgSuccess(`转账成功，流水号 ${res.data}`)
      showTransfer.value = false
      getFlowList(); getAccountList()
    })
  })
}

function handleReconcile() {
  const ids = flowSelection.value.map(f => f.flowId)
  proxy.$modal.confirm(`是否确认将选中的 ${ids.length} 笔流水标记为已对账？`).then(() => {
    return reconcileFlow(ids)
  }).then(() => {
    proxy.$modal.msgSuccess('已标记为对账')
    getFlowList()
  }).catch(() => {})
}

function handleExport() {
  proxy.download('/fin/fund/export', { ...flowQuery.value }, `资金流水_${new Date().getTime()}.xlsx`)
}

getStatements()
getAccountList()
getFlowList()
</script>

<style scoped>
.hint-text { font-size: 12px; }
.form-tip { font-size: 11px; color: var(--color-text-tertiary); line-height: 1.4; margin-top: 2px; }

.amount-cell { font-variant-numeric: tabular-nums; }
.amount-cell.strong { font-weight: 600; }
.account-no { font-family: ui-monospace, Menlo, Consolas, monospace; letter-spacing: 0.5px; }
.text-muted { color: var(--color-text-tertiary); }
.text-in { color: #16a34a; }
.text-out { color: #b85450; }


/* ===== 以下为 EDOP 规范样式（从 cost 页复制，前缀 .fin-fund-page）===== */

/* ===== 7.1 Design Tokens ===== */
.fin-fund-page {
  --page-bg: #f5f7fa;
  --card-bg: #ffffff;
  --border-color: #e4e7ed;
  --border-light: #ebeef5;
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --primary-color: #409eff;
  --primary-light: #ecf5ff;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --shadow-card: 0 1px 4px rgba(0, 21, 41, 0.08);

  padding: 16px;
  background: var(--page-bg);
  min-height: calc(100vh - 84px);
}

/* ===== 卡片容器（对齐应收管理页面） ===== */
.fin-fund-page .surface {
  background: #fff;
  border: 1px solid var(--ink-200);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  margin-bottom: 8px;
}
.fin-fund-page .surface.filter-card { overflow: visible; }

/* ===== 筛选条件卡片（对齐应收管理页面） ===== */
.fin-fund-page .filter-card { padding:14px 20px 16px; }
.fin-fund-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.fin-fund-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.fin-fund-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.fin-fund-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.fin-fund-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.fin-fund-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.fin-fund-page .filter-card .filter-buttons { display:flex; gap:8px; }
.fin-fund-page .adv-link { display:flex; align-items:center; gap:4px; font-size:13px; color:var(--brand-600); cursor:pointer; user-select:none; transition:color .15s var(--ease-out); }
.fin-fund-page .adv-link:hover { color:var(--brand-700); }
.fin-fund-page .adv-link .el-icon { transition:transform .2s var(--ease-out); }
.fin-fund-page .adv-link.is-open .el-icon { transform:rotate(180deg); }

/* ===== Form Field（对齐应收管理页面） ===== */
.fin-fund-page .field { display:flex; flex-direction:column; gap:6px; }
.fin-fund-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.fin-fund-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.fin-fund-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.fin-fund-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.fin-fund-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.fin-fund-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.fin-fund-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.fin-fund-page .field .control :deep(.el-select) { width:100%; }
.fin-fund-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.fin-fund-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.fin-fund-page .field .control :deep(.el-date-editor) { width:100%; }
.fin-fund-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.fin-fund-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.fin-fund-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== 工具栏（对齐应收管理页面） ===== */
.fin-fund-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.fin-fund-page .toolbar .left { display:flex; gap:8px; align-items:center; flex-wrap:wrap; }
.fin-fund-page .toolbar .right { display:flex; gap:8px; align-items:center; }

/* ===== 表格容器 ===== */
.fin-fund-page .table-wrap {
  width: 100%;
}
.fin-fund-page :deep(.el-table) {
  --el-table-border-color: var(--border-light);
  font-size: 13px;
}
.fin-fund-page :deep(.el-table th.el-table__cell) {
  background: #f8f9fb;
  color: var(--text-primary);
  font-weight: 600;
}
.fin-fund-page :deep(.el-table .col-mono .cell) {
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 12px;
}
.fin-fund-page .pagination-container {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}

/* ===== 徽章 ===== */
.fin-fund-page .badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 18px;
  white-space: nowrap;
}
.fin-fund-page .badge .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}
.fin-fund-page .badge.blue { background: #ecf5ff; color: #409eff; }
.fin-fund-page .badge.green { background: #f0f9eb; color: #67c23a; }
.fin-fund-page .badge.amber { background: #fdf6ec; color: #e6a23c; }
.fin-fund-page .badge.red { background: #fef0f0; color: #f56c6c; }
.fin-fund-page .badge.violet { background: #f4f0ff; color: #7c5cff; }
.fin-fund-page .badge.gray { background: #f4f4f5; color: #909399; }

/* ===== 操作列（5.5.3） ===== */
.fin-fund-page :deep(.col-action) {
  padding: 6px 4px !important;
}
.fin-fund-page :deep(.col-action .cell) {
  display: flex;
  justify-content: center;
  padding: 0;
}
.fin-fund-page .action-btn-row {
  display: inline-flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0;
}
.fin-fund-page :deep(.col-action .el-button) {
  padding: 2px 4px;
  margin: 0 2px;
  white-space: nowrap;
  justify-content: center;
}
.fin-fund-page :deep(.col-action .el-button + .el-button) {
  margin-left: 2px;
}


/* ===== 金额与状态文字 ===== */
.fin-fund-page .amount-cell {
  font-variant-numeric: tabular-nums;
  color: var(--text-primary);
}
.fin-fund-page .amount-cell.strong {
  font-weight: 600;
  color: var(--primary-color);
}
.fin-fund-page .text-muted { color: var(--text-secondary); }
.fin-fund-page .text-success { color: var(--success-color); }
.fin-fund-page .text-warning { color: var(--warning-color); }
.fin-fund-page .text-danger { color: var(--danger-color); }
.fin-fund-page .form-tip {
  font-size: 12px;
  color: var(--text-secondary);
  padding-left: 120px;
  margin-top: -4px;
}
.fin-fund-page .hint-text { font-size: 12px; font-weight: normal; }

/* ===== 弹窗（rd-dialog） ===== */
.fin-fund-page :deep(.rd-dialog .el-dialog__header) {
  padding: 0;
  margin: 0;
}
.fin-fund-page :deep(.rd-dialog .el-dialog__headerbtn) {
  top: 14px;
  right: 16px;
}
.fin-fund-page :deep(.rd-dialog .el-dialog__body) {
  padding: 20px 24px;
}
.fin-fund-page :deep(.rd-detail-header) {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 24px;
  background: linear-gradient(135deg, #eef4ff 0%, #f7faff 100%);
  border-bottom: 1px solid var(--border-light);
  color: var(--primary-color);
}
.fin-fund-page :deep(.rd-detail-title) {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

/* ===== 详情页 rd- 结构 ===== */
.fin-fund-page .rd-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.fin-fund-page .rd-card {
  background: var(--card-bg);
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 16px 20px;
  animation: rdFadeIn 0.3s ease;
}
.fin-fund-page .rd-card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid var(--primary-color);
  line-height: 18px;
}
.fin-fund-page .rd-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 24px;
}
.fin-fund-page .rd-item {
  min-width: 0;
}
.fin-fund-page .rd-item-full {
  grid-column: 1 / -1;
}
.fin-fund-page .rd-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}
.fin-fund-page .rd-value {
  font-size: 14px;
  color: var(--text-primary);
  word-break: break-all;
}
.fin-fund-page .rd-amount {
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  color: var(--primary-color);
}
.fin-fund-page .rd-empty {
  padding: 32px 0;
  text-align: center;
  color: var(--text-secondary);
  font-size: 13px;
}
@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: none; }
}

/* ===== 响应式（对齐应收管理页面） ===== */
@media (max-width:1100px) { .fin-fund-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) {
  .fin-fund-page .filter-card .filter-bar { grid-template-columns:1fr; }
  .fin-fund-page .toolbar { flex-wrap:wrap; gap:10px; }
  .fin-fund-page .rd-grid { grid-template-columns:1fr; }
}

</style>