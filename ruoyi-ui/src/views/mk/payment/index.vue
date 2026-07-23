<template>
  <div class="app-container">
    <!-- 回款统计卡片 -->
    <el-row :gutter="12" class="mb8" v-if="stats">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">总计划回款</div>
          <div class="stat-value">{{ formatMoney(stats.totalPlanAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card stat-success">
          <div class="stat-label">已回款</div>
          <div class="stat-value">{{ formatMoney(stats.totalActualAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card stat-warning">
          <div class="stat-label">待回款</div>
          <div class="stat-value">{{ formatMoney(stats.totalPendingAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card stat-danger">
          <div class="stat-label">逾期金额</div>
          <div class="stat-value">{{ formatMoney(stats.totalOverdueAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">回款率</div>
          <div class="stat-value">{{ stats.paymentRate }}%</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">逾期笔数</div>
          <div class="stat-value">{{ stats.overdueCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="合同编号" prop="contractNo">
        <el-input v-model="queryParams.contractNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="回款状态" prop="paymentStatus">
        <el-select v-model="queryParams.paymentStatus" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_payment_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="逾期" prop="overdueFlag">
        <el-select v-model="queryParams.overdueFlag" placeholder="请选择" clearable style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="未逾期" value="0" />
          <el-option label="已逾期" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:payment:add']">新增回款计划</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:payment:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:payment:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="合同编号" prop="contractNo" :width="colWidth('contractNo', 150)" resizable />
      <el-table-column label="客户名称" prop="customerName" show-overflow-tooltip />
      <el-table-column label="期次" prop="periodNo" :width="colWidth('periodNo', 80)" resizable align="center" />
      <el-table-column label="计划回款金额" prop="planAmount" :width="colWidth('planAmount', 140)" resizable align="right" />
      <el-table-column label="计划回款日期" prop="planDate" :width="colWidth('planDate', 130)" resizable />
      <el-table-column label="实际回款金额" prop="actualAmount" :width="colWidth('actualAmount', 140)" resizable align="right" />
      <el-table-column label="实际回款日期" prop="actualDate" :width="colWidth('actualDate', 130)" resizable />
      <el-table-column label="回款方式" prop="paymentMethod" :width="colWidth('paymentMethod', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_payment_method" :value="scope.row.paymentMethod" /></template>
      </el-table-column>
      <el-table-column label="状态" prop="paymentStatus" :width="colWidth('paymentStatus', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_payment_status" :value="scope.row.paymentStatus" /></template>
      </el-table-column>
      <el-table-column label="逾期" prop="overdueFlag" :width="colWidth('overdueFlag', 80)" resizable align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.overdueFlag === '1'" type="danger" size="small">逾期</el-tag>
          <span v-else style="color: #c0c4cc">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="success" icon="Money" @click="handleRecord(scope.row)" v-if="scope.row.paymentStatus !== '1'" v-hasPermi="['marketing:payment:edit']">回款登记</el-button>
          <el-button link type="warning" icon="Check" @click="handleConfirm(scope.row)" v-if="scope.row.paymentStatus !== '1'" v-hasPermi="['marketing:payment:confirm']">回款确认</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增回款计划对话框 -->
    <el-dialog v-model="addOpen" width="800px" append-to-body draggable class="payment-add-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="1" x2="12" y2="23"/>
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">新增回款计划</span>
        </div>
      </template>
      <el-form ref="addRef" :model="addForm" :rules="addRules" label-width="120px">
        <div class="rd-page">
          <!-- 关联信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('addRelation')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                    <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                  </svg>
                </span>
                关联信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.addRelation }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.addRelation">
              <el-row>
                <el-col :span="12"><el-form-item label="关联客户" prop="customerId">
                  <el-select v-model="addForm.customerId" filterable placeholder="请选择客户" style="width: 100%" @change="onCustomerChange">
                    <el-option v-for="c in customerOptions" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="关联合同" prop="contractId">
                  <el-select v-model="addForm.contractId" filterable clearable placeholder="可选" style="width: 100%" @change="onContractChange">
                    <el-option v-for="c in contractOptions" :key="c.contractId" :label="c.contractNo + ' - ' + c.contractName" :value="c.contractId" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 计划信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('addPlan')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                    <line x1="16" y1="2" x2="16" y2="6"/>
                    <line x1="8" y1="2" x2="8" y2="6"/>
                    <line x1="3" y1="10" x2="21" y2="10"/>
                  </svg>
                </span>
                计划信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.addPlan }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.addPlan">
              <el-row>
                <el-col :span="8"><el-form-item label="期次" prop="periodNo"><el-input-number v-model="addForm.periodNo" :min="1" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="计划回款金额" prop="planAmount"><el-input-number v-model="addForm.planAmount" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="计划回款日期" prop="planDate"><el-date-picker v-model="addForm.planDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="回款方式" prop="paymentMethod">
                  <el-select v-model="addForm.paymentMethod" placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in marketing_payment_method" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="收款银行账户" prop="bankAccount"><el-input v-model="addForm.bankAccount" placeholder="请输入银行账户" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('addOther')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="16" x2="12" y2="12"/>
                    <line x1="12" y1="8" x2="12.01" y2="8"/>
                  </svg>
                </span>
                其他信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.addOther }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.addOther">
              <el-form-item label="备注" prop="remark"><el-input v-model="addForm.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAdd">确 定</el-button>
        <el-button @click="addOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 回款登记对话框（多次回款） -->
    <el-dialog v-model="recordOpen" width="1000px" append-to-body draggable class="payment-record-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="2" y="5" width="20" height="14" rx="2"/>
              <line x1="2" y1="10" x2="22" y2="10"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">回款登记</span>
          <div class="rd-detail-header-sub" v-if="recordForm.contractNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">合同：{{ recordForm.contractNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 计划信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('recordPlan')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </span>
              计划信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.recordPlan }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.recordPlan">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ recordForm.contractNo }}</div></div>
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ recordForm.customerName }}</div></div>
              <div class="rd-item"><span class="rd-label">期次</span><div class="rd-value">{{ recordForm.periodNo }}</div></div>
              <div class="rd-item"><span class="rd-label">计划回款金额</span><div class="rd-value rd-value--large rd-amount">￥{{ formatMoney(recordForm.planAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">已回款金额</span><div class="rd-value rd-amount">￥{{ formatMoney(recordForm.actualAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">待回款金额</span><div class="rd-value rd-value--large rd-amount">￥{{ formatMoney(recordForm.planAmount - recordForm.actualAmount) }}</div></div>
            </div>
          </div>
        </section>

        <!-- 回款登记 -->
        <section class="rd-card" v-if="recordForm.paymentStatus !== '1'">
          <div class="rd-card-header" @click="toggleCard('recordForm')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="12" y1="18" x2="12" y2="12"/>
                  <line x1="9" y1="15" x2="15" y2="15"/>
                </svg>
              </span>
              回款登记
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.recordForm }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.recordForm">
            <el-form ref="recordFormRef" :model="recordFormData" :rules="recordRules" label-width="100px">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="回款金额" prop="thisAmount">
                    <el-input-number v-model="recordFormData.thisAmount" :min="0" :precision="2" :controls="true" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="回款日期" prop="paymentDate">
                    <el-date-picker v-model="recordFormData.paymentDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="回款方式" prop="paymentMethod">
                    <el-select v-model="recordFormData.paymentMethod" placeholder="请选择" style="width: 100%">
                      <el-option v-for="d in marketing_payment_method" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="银行账户" prop="bankAccount">
                    <el-input v-model="recordFormData.bankAccount" placeholder="请输入银行账户" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="收款凭证" prop="receiptAttachment">
                    <file-upload v-model="recordFormData.receiptAttachment" :fileSize="20" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                    <el-input v-model="recordFormData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
        </section>

        <!-- 回款完成提示 -->
        <section class="rd-card" v-if="recordForm.paymentStatus === '1'">
          <div class="rd-card-body">
            <el-result icon="success" title="回款已完成" sub-title="该回款计划已全部回款，无需继续登记">
              <template #extra>
                <el-button @click="recordOpen = false">关 闭</el-button>
              </template>
            </el-result>
          </div>
        </section>

        <!-- 已登记的回款记录 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('recordHistory')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M3 3v5h5"/>
                  <path d="M3.05 13A9 9 0 1 0 6 5.3L3 8"/>
                  <path d="M12 7v5l4 2"/>
                </svg>
              </span>
              已登记的回款记录
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.recordHistory }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.recordHistory">
            <el-table border :data="existingRecords" size="small" v-if="existingRecords.length > 0">
              <el-table-column label="回款金额" prop="thisAmount" width="120" align="center" />
              <el-table-column label="回款日期" prop="paymentDate" width="120" align="center" />
              <el-table-column label="回款方式" width="100" align="center">
                <template #default="scope"><dict-tag :options="marketing_payment_method" :value="scope.row.paymentMethod" /></template>
              </el-table-column>
              <el-table-column label="确认状态" width="100" align="center">
                <template #default="scope">
                  <el-tag v-if="scope.row.confirmStatus === '0'" type="warning" size="small">待确认</el-tag>
                  <el-tag v-else-if="scope.row.confirmStatus === '1'" type="success" size="small">已确认</el-tag>
                  <el-tag v-else type="danger" size="small">已驳回</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="确认人" prop="confirmBy" width="100" align="center" />
              <el-table-column label="凭证" width="80" align="center">
                <template #default="scope">
                  <el-link v-if="scope.row.receiptAttachment" :href="baseUrl + scope.row.receiptAttachment" target="_blank" type="primary"><el-icon><View /></el-icon></el-link>
                  <span v-else style="color: #c0c4cc">-</span>
                </template>
              </el-table-column>
            </el-table>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              <p class="rd-empty-text">暂无回款记录</p>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitRecord" v-if="recordForm.paymentStatus !== '1'">提交回款</el-button>
        <el-button @click="recordOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="payment-detail-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="1" x2="12" y2="23"/>
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">回款计划详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.contractNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">合同：{{ viewForm.contractNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 关联合同 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewContract')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                  <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                </svg>
              </span>
              关联合同
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewContract }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewContract">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ viewForm.contractNo }}</div></div>
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ viewForm.customerName }}</div></div>
            </div>
          </div>
        </section>

        <!-- 回款计划信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewPlan')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </span>
              回款计划信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewPlan }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewPlan">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">期次</span><div class="rd-value">{{ viewForm.periodNo }}</div></div>
              <div class="rd-item"><span class="rd-label">计划回款金额</span><div class="rd-value rd-value--large rd-amount">￥{{ formatMoney(viewForm.planAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">计划回款日期</span><div class="rd-value">{{ viewForm.planDate }}</div></div>
              <div class="rd-item"><span class="rd-label">回款方式</span><div class="rd-value"><dict-tag :options="marketing_payment_method" :value="viewForm.paymentMethod" /></div></div>
              <div class="rd-item"><span class="rd-label">收款银行账户</span><div class="rd-value">{{ viewForm.bankAccount }}</div></div>
              <div class="rd-item"><span class="rd-label">逾期标志</span><div class="rd-value"><el-tag v-if="viewForm.overdueFlag === '1'" type="danger" size="small">已逾期</el-tag><span v-else>未逾期</span></div></div>
            </div>
          </div>
        </section>

        <!-- 实际回款信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewActual')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                  <polyline points="22 4 12 14.01 9 11.01"/>
                </svg>
              </span>
              实际回款信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewActual }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewActual">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">实际回款金额</span><div class="rd-value rd-value--large rd-amount">￥{{ formatMoney(viewForm.actualAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">实际回款日期</span><div class="rd-value">{{ viewForm.actualDate }}</div></div>
              <div class="rd-item"><span class="rd-label">回款状态</span><div class="rd-value"><dict-tag :options="marketing_payment_status" :value="viewForm.paymentStatus" /></div></div>
            </div>
          </div>
        </section>

        <!-- 回款记录明细 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewRecords')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="8" y1="6" x2="21" y2="6"/>
                  <line x1="8" y1="12" x2="21" y2="12"/>
                  <line x1="8" y1="18" x2="21" y2="18"/>
                  <line x1="3" y1="6" x2="3.01" y2="6"/>
                  <line x1="3" y1="12" x2="3.01" y2="12"/>
                  <line x1="3" y1="18" x2="3.01" y2="18"/>
                </svg>
              </span>
              回款记录明细
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewRecords }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewRecords">
            <el-table border :data="viewForm.recordList" size="small" v-if="viewForm.recordList && viewForm.recordList.length > 0">
              <el-table-column label="回款金额" prop="thisAmount" width="120" align="center" />
              <el-table-column label="回款日期" prop="paymentDate" width="120" align="center" />
              <el-table-column label="回款方式" width="100" align="center">
                <template #default="scope"><dict-tag :options="marketing_payment_method" :value="scope.row.paymentMethod" /></template>
              </el-table-column>
              <el-table-column label="确认状态" width="100" align="center">
                <template #default="scope">
                  <el-tag v-if="scope.row.confirmStatus === '0'" type="warning" size="small">待确认</el-tag>
                  <el-tag v-else-if="scope.row.confirmStatus === '1'" type="success" size="small">已确认</el-tag>
                  <el-tag v-else type="danger" size="small">已驳回</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="确认人" prop="confirmBy" width="100" align="center" />
              <el-table-column label="备注" prop="remark" show-overflow-tooltip />
            </el-table>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              <p class="rd-empty-text">暂无回款记录</p>
            </div>
          </div>
        </section>

        <!-- 其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('viewOther')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="16" x2="12" y2="12"/>
                  <line x1="12" y1="8" x2="12.01" y2="8"/>
                </svg>
              </span>
              其他信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewOther }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.viewOther">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无备注' }}</div></div>
            </div>
          </div>
        </section>
      </div>
    </el-dialog>

    <!-- 回款确认对话框 -->
    <el-dialog v-model="confirmOpen" width="1000px" append-to-body draggable class="payment-confirm-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 11l3 3L22 4"/>
              <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">回款确认</span>
          <div class="rd-detail-header-sub" v-if="confirmPlanForm.contractNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">合同：{{ confirmPlanForm.contractNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 计划信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('confirmPlan')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </span>
              计划信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.confirmPlan }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.confirmPlan">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ confirmPlanForm.contractNo }}</div></div>
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ confirmPlanForm.customerName }}</div></div>
              <div class="rd-item"><span class="rd-label">期次</span><div class="rd-value">{{ confirmPlanForm.periodNo }}</div></div>
              <div class="rd-item"><span class="rd-label">计划回款金额</span><div class="rd-value rd-value--large rd-amount">￥{{ formatMoney(confirmPlanForm.planAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">已确认金额</span><div class="rd-value rd-amount">￥{{ formatMoney(confirmPlanForm.actualAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">待确认金额</span><div class="rd-value rd-value--large rd-amount">￥{{ formatMoney((confirmPlanForm.planAmount || 0) - (confirmPlanForm.actualAmount || 0)) }}</div></div>
            </div>
          </div>
        </section>

        <!-- 待确认的回款记录 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('confirmRecords')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="12" y1="18" x2="12" y2="12"/>
                  <line x1="9" y1="15" x2="15" y2="15"/>
                </svg>
              </span>
              回款记录确认
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.confirmRecords }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.confirmRecords">
            <el-table border :data="confirmRecords" size="small" v-if="confirmRecords.length > 0">
              <el-table-column label="回款金额" prop="thisAmount" width="120" align="center" />
              <el-table-column label="回款日期" prop="paymentDate" width="120" align="center" />
              <el-table-column label="回款方式" width="100" align="center">
                <template #default="scope"><dict-tag :options="marketing_payment_method" :value="scope.row.paymentMethod" /></template>
              </el-table-column>
              <el-table-column label="确认状态" width="100" align="center">
                <template #default="scope">
                  <el-tag v-if="scope.row.confirmStatus === '0'" type="warning" size="small">待确认</el-tag>
                  <el-tag v-else-if="scope.row.confirmStatus === '1'" type="success" size="small">已确认</el-tag>
                  <el-tag v-else type="danger" size="small">已驳回</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="确认人" prop="confirmBy" width="100" align="center" />
              <el-table-column label="凭证" width="80" align="center">
                <template #default="scope">
                  <el-link v-if="scope.row.receiptAttachment" :href="baseUrl + scope.row.receiptAttachment" target="_blank" type="primary"><el-icon><View /></el-icon></el-link>
                  <span v-else style="color: #c0c4cc">-</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160" align="center">
                <template #default="scope">
                  <el-button v-if="scope.row.confirmStatus === '0'" link type="success" icon="Check" size="small" @click="handleConfirmRecord(scope.row, '1')" v-hasPermi="['marketing:payment:confirm']">确认</el-button>
                  <el-button v-if="scope.row.confirmStatus === '0'" link type="danger" icon="Close" size="small" @click="handleConfirmRecord(scope.row, '2')" v-hasPermi="['marketing:payment:confirm']">驳回</el-button>
                  <el-button v-if="scope.row.confirmStatus === '0'" link type="danger" icon="Delete" size="small" @click="handleDeleteConfirmRecord(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              <p class="rd-empty-text">暂无待确认的回款记录</p>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="confirmOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkPayment">
import { listPayment, getPayment, addPayment, delPayment, paymentStatistics, listPaymentRecord, addPaymentRecord, delPaymentRecord, confirmPaymentRecord } from '@/api/mk/payment'
import { listContract } from '@/api/mk/contract'
import { listCustomer } from '@/api/mk/customer'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_payment_index')
const { marketing_payment_status, marketing_payment_method } = proxy.useDict('marketing_payment_status', 'marketing_payment_method')

const list = ref([])
const open = ref(false)
const addOpen = ref(false)
const recordOpen = ref(false)
const viewOpen = ref(false)
const confirmOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const viewForm = ref({})
const stats = ref(null)
const customerOptions = ref([])
const contractOptions = ref([])
const existingRecords = ref([])
const recordForm = ref({})
const recordFormData = ref({})
const confirmPlanForm = ref({})
const confirmRecords = ref([])
const collapsedCards = reactive({ addRelation: false, addPlan: false, addOther: false, recordPlan: false, recordForm: false, recordHistory: false, viewContract: false, viewPlan: false, viewActual: false, viewRecords: false, viewOther: false, confirmPlan: false, confirmRecords: false })
function toggleCard(name) { collapsedCards[name] = !collapsedCards[name] }
const baseUrl = import.meta.env.VITE_APP_BASE_API

const data = reactive({
  form: {},
  addForm: {},
  queryParams: { pageNum: 1, pageSize: 10, contractNo: undefined, customerName: undefined, paymentStatus: undefined, overdueFlag: undefined },
  addRules: {
    customerId: [{ required: true, message: '请选择关联客户', trigger: 'change' }],
    planAmount: [{ required: true, message: '计划回款金额不能为空', trigger: 'blur' }],
    planDate: [{ required: true, message: '请选择计划回款日期', trigger: 'change' }]
  },
  recordRules: {
    thisAmount: [{ required: true, message: '回款金额不能为空', trigger: 'blur' }],
    paymentDate: [{ required: true, message: '请选择回款日期', trigger: 'change' }],
    paymentMethod: [{ required: true, message: '请选择回款方式', trigger: 'change' }]
  }
})
const { queryParams, addForm, addRules, recordRules } = toRefs(data)

function formatMoney(val) {
  if (val == null) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getList() {
  loading.value = true
  listPayment(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}

function loadStats() {
  paymentStatistics().then(res => { stats.value = res.data })
}

function getCustomerOptions() {
  listCustomer({ pageNum: 1, pageSize: 9999 }).then(res => { customerOptions.value = res.rows })
}

function getContractOptions() {
  listContract({ pageNum: 1, pageSize: 9999 }).then(res => { contractOptions.value = res.rows })
}

function onCustomerChange(customerId) {
  if (customerId) {
    const customer = customerOptions.value.find(c => c.customerId === customerId)
    if (customer) addForm.value.customerName = customer.customerName
  }
}

function onContractChange(contractId) {
  if (contractId) {
    const contract = contractOptions.value.find(c => c.contractId === contractId)
    if (contract) {
      addForm.value.contractNo = contract.contractNo
      addForm.value.customerId = contract.customerId
      addForm.value.customerName = contract.customerName
    }
  }
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.planId); single.value = selection.length !== 1; multiple.value = !selection.length }

function handleAdd() {
  addForm.value = { customerId: undefined, contractId: undefined, periodNo: 1, planAmount: 0, planDate: undefined, paymentMethod: '0', bankAccount: undefined, remark: undefined }
  proxy.resetForm('addRef')
  addOpen.value = true
}

function submitAdd() {
  proxy.$refs['addRef'].validate(valid => {
    if (valid) {
      addPayment(addForm.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        addOpen.value = false
        getList()
        loadStats()
      })
    }
  })
}

function handleDelete(row) {
  const planIds = row.planId || ids.value
  proxy.$modal.confirm('确认删除选中的回款计划？').then(() => delPayment(planIds)).then(() => { getList(); loadStats(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}

function handleView(row) {
  getPayment(row.planId).then(res => { viewForm.value = res.data; viewOpen.value = true })
}

function handleRecord(row) {
  getPayment(row.planId).then(res => {
    recordForm.value = res.data
    existingRecords.value = res.data.recordList || []
    // 初始化回款登记表单
    const remainingAmount = (res.data.planAmount || 0) - (res.data.actualAmount || 0)
    recordFormData.value = {
      planId: res.data.planId,
      contractId: res.data.contractId,
      customerId: res.data.customerId,
      thisAmount: remainingAmount > 0 ? remainingAmount : 0,
      paymentDate: undefined,
      paymentMethod: res.data.paymentMethod || '0',
      bankAccount: res.data.bankAccount,
      receiptAttachment: undefined,
      remark: undefined,
      confirmStatus: '0'
    }
    proxy.resetForm('recordFormRef')
    recordOpen.value = true
  })
}

function submitRecord() {
  proxy.$refs['recordFormRef'].validate(valid => {
    if (!valid) return
    const data = { ...recordFormData.value }
    if (!data.thisAmount || data.thisAmount <= 0) {
      proxy.$modal.msgWarning('回款金额必须大于0')
      return
    }
    addPaymentRecord(data).then(() => {
      proxy.$modal.msgSuccess('回款登记成功')
      getList()
      loadStats()
      // 刷新对话框数据
      getPayment(recordForm.value.planId).then(res => {
        recordForm.value = res.data
        existingRecords.value = res.data.recordList || []
        // 如果回款已完成，显示完成提示，不重置表单
        if (res.data.paymentStatus === '1') {
          proxy.$modal.msgSuccess('该回款计划已全部回款完成')
        } else {
          // 重置表单，准备下一次登记
          const remainingAmount = (res.data.planAmount || 0) - (res.data.actualAmount || 0)
          recordFormData.value = {
            planId: res.data.planId,
            contractId: res.data.contractId,
            customerId: res.data.customerId,
            thisAmount: remainingAmount > 0 ? remainingAmount : 0,
            paymentDate: undefined,
            paymentMethod: res.data.paymentMethod || '0',
            bankAccount: res.data.bankAccount,
            receiptAttachment: undefined,
            remark: undefined,
            confirmStatus: '0'
          }
        }
      })
    })
  })
}

function handleConfirmRecord(row, confirmStatus) {
  const msg = confirmStatus === '1' ? '确认' : '驳回'
  proxy.$modal.confirm('确认' + msg + '该回款记录？').then(() => confirmPaymentRecord(row.recordId, confirmStatus, '')).then(() => {
    proxy.$modal.msgSuccess(msg + '成功')
    // 刷新回款确认对话框数据
    refreshConfirmDialog()
    getList()
    loadStats()
  }).catch(() => {})
}

function handleDeleteConfirmRecord(row) {
  proxy.$modal.confirm('确认删除该回款记录？').then(() => delPaymentRecord(row.recordId)).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    refreshConfirmDialog()
    getList()
    loadStats()
  }).catch(() => {})
}

/** 刷新回款确认对话框数据 */
function refreshConfirmDialog() {
  if (confirmPlanForm.value.planId) {
    getPayment(confirmPlanForm.value.planId).then(res => {
      confirmPlanForm.value = res.data
      confirmRecords.value = res.data.recordList || []
      // 如果回款已完成，关闭确认对话框
      if (res.data.paymentStatus === '1') {
        proxy.$modal.msgSuccess('该回款计划已全部回款完成')
        confirmOpen.value = false
      }
    })
  }
}

/** 打开回款确认对话框 */
function handleConfirm(row) {
  getPayment(row.planId).then(res => {
    confirmPlanForm.value = res.data
    confirmRecords.value = res.data.recordList || []
    confirmOpen.value = true
  })
}

function handleExport() { proxy.download('mk/payment/export', { ...queryParams.value }, `payment_${new Date().getTime()}.xlsx`) }

getCustomerOptions()
getContractOptions()
getList()
loadStats()
</script>

<style scoped>
.stat-card { text-align: center; }
.stat-label { font-size: 13px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 22px; font-weight: bold; color: #303133; }
.stat-success .stat-value { color: #67c23a; }
.stat-warning .stat-value { color: #e6a23c; }
.stat-danger .stat-value { color: #f56c6c; }

/* ===== 卡片式样式 ===== */
:deep(.payment-add-dialog .el-dialog__header), :deep(.payment-record-dialog .el-dialog__header), :deep(.payment-detail-dialog .el-dialog__header), :deep(.payment-confirm-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.payment-add-dialog .el-dialog__headerbtn), :deep(.payment-record-dialog .el-dialog__headerbtn), :deep(.payment-detail-dialog .el-dialog__headerbtn), :deep(.payment-confirm-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.payment-add-dialog .el-dialog__headerbtn .el-dialog__close), :deep(.payment-record-dialog .el-dialog__headerbtn .el-dialog__close), :deep(.payment-detail-dialog .el-dialog__headerbtn .el-dialog__close), :deep(.payment-confirm-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.payment-add-dialog .el-dialog__headerbtn:hover .el-dialog__close), :deep(.payment-record-dialog .el-dialog__headerbtn:hover .el-dialog__close), :deep(.payment-detail-dialog .el-dialog__headerbtn:hover .el-dialog__close), :deep(.payment-confirm-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.payment-add-dialog .el-dialog__body), :deep(.payment-record-dialog .el-dialog__body), :deep(.payment-detail-dialog .el-dialog__body), :deep(.payment-confirm-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

.rd-detail-header { display: flex; align-items: center; gap: 10px; padding: 8px 16px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -25px; right: -10px; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.12) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 8px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 2px 8px rgb(0 0 0 / 0.1); }
.rd-detail-header-title { font-size: 16px; font-weight: 700; color: #fff; letter-spacing: -0.02em; white-space: nowrap; }
.rd-detail-header-sub { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.rd-detail-header-divider { width: 1px; height: 16px; background: rgb(255 255 255 / 0.3); flex-shrink: 0; }
.rd-detail-header-no { font-size: 12px; font-weight: 500; color: rgb(255 255 255 / 0.85); font-variant-numeric: tabular-nums; white-space: nowrap; }

.rd-page { max-width: 940px; margin: 0 auto; }

.rd-card { background: #fff; border-radius: 12px; border: 1px solid #e5e7eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); overflow: hidden; margin-bottom: 8px; transition: box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1); animation: rdFadeIn 0.4s ease-out forwards; }
.rd-card:hover { box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1); }
.rd-card:last-child { margin-bottom: 0; }
.rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; background: linear-gradient(to right, #f9fafb, #fff); border-bottom: 1px solid #f3f4f6; cursor: pointer; user-select: none; }
.rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: #111827; }
.rd-card-icon { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 8px; background: #fff; border: 1px solid #e5e7eb; color: #2563eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); }
.rd-card-body { padding: 14px 16px; }

.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.rd-value--large { font-size: 18px; font-weight: 700; }
.rd-value--muted { color: #9ca3af; font-style: italic; }
.rd-amount { font-variant-numeric: tabular-nums; font-weight: 700; color: #111827; }

.rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border-radius: 6px; border: none; background: transparent; color: #9ca3af; cursor: pointer; transition: all 0.15s ease; flex-shrink: 0; }
.rd-collapse-btn:hover { background: #f3f4f6; color: #4b5563; }
.rd-collapse-btn svg { transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.rd-collapse-btn.is-collapsed svg { transform: rotate(-90deg); }

.rd-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 20px; text-align: center; color: #9ca3af; background: #f9fafb; border-radius: 12px; border: 1px dashed #e5e7eb; }
.rd-empty-icon { margin-bottom: 4px; color: #d1d5db; }
.rd-empty-text { font-size: 14px; font-weight: 500; margin: 0; }

@keyframes rdFadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.rd-card:nth-child(2) { animation-delay: 0.06s; }
.rd-card:nth-child(3) { animation-delay: 0.12s; }
.rd-card:nth-child(4) { animation-delay: 0.18s; }

@media (max-width: 768px) { .rd-grid { grid-template-columns: 1fr; } .rd-card-header { padding: 8px 12px; } .rd-card-body { padding: 12px; } }
</style>
