<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="合同编号" prop="contractNo">
        <el-input v-model="queryParams.contractNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="合同名称" prop="contractName">
        <el-input v-model="queryParams.contractName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="合同状态" prop="contractStatus">
        <el-select v-model="queryParams.contractStatus" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_contract_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:contract:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:contract:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:contract:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="合同编号" prop="contractNo" :width="colWidth('contractNo', 150)" resizable />
      <el-table-column label="合同名称" prop="contractName" show-overflow-tooltip />
      <el-table-column label="客户名称" prop="customerName" :width="colWidth('customerName', 180)" resizable show-overflow-tooltip />
      <el-table-column label="合同类型" prop="contractType" :width="colWidth('contractType', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_contract_type" :value="scope.row.contractType" /></template>
      </el-table-column>
      <el-table-column label="合同金额" prop="contractAmount" :width="colWidth('contractAmount', 120)" resizable align="right" />
      <el-table-column label="签约日期" prop="signDate" :width="colWidth('signDate', 120)" resizable />
      <el-table-column label="付款方式" prop="paymentMethod" :width="colWidth('paymentMethod', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_payment_method" :value="scope.row.paymentMethod" /></template>
      </el-table-column>
      <el-table-column label="状态" prop="contractStatus" :width="colWidth('contractStatus', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_contract_status" :value="scope.row.contractStatus" /></template>
      </el-table-column>
      <el-table-column label="负责人" prop="userName" :width="colWidth('userName', 100)" resizable />
      <el-table-column label="操作" width="320" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <!-- 草稿：可修改 -->
          <el-button v-if="scope.row.contractStatus === '0'" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:contract:edit']">修改</el-button>
          <!-- 草稿：提交审批 -->
          <el-button v-if="scope.row.contractStatus === '0'" link type="warning" icon="Promotion" @click="handleSubmit(scope.row)" v-hasPermi="['marketing:contract:edit']">提交审批</el-button>
          <!-- 审批中：审批 -->
          <el-button v-if="scope.row.contractStatus === '1'" link type="warning" icon="DocumentChecked" @click="handleApprove(scope.row)" v-hasPermi="['marketing:contract:approve']">审批</el-button>
          <!-- 变更审批中：审批变更 -->
          <el-button v-if="scope.row.contractStatus === '5'" link type="warning" icon="DocumentChecked" @click="handleView(scope.row, true)" v-hasPermi="['marketing:contract:change']">审批</el-button>
          <!-- 已生效：变更 -->
          <el-button v-if="scope.row.contractStatus === '2'" link type="warning" icon="EditPen" @click="handleChange(scope.row)" v-hasPermi="['marketing:contract:change']">变更</el-button>
          <!-- 已生效/已过期：续签 -->
          <el-button v-if="scope.row.contractStatus === '2' || scope.row.contractStatus === '3'" link type="primary" icon="RefreshRight" @click="handleRenew(scope.row)" v-hasPermi="['marketing:contract:renew']">续签</el-button>
          <!-- 已生效：终止 -->
          <el-button v-if="scope.row.contractStatus === '2'" link type="danger" icon="CircleClose" @click="handleTerminate(scope.row)" v-hasPermi="['marketing:contract:edit']">终止</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="1200px" append-to-body draggable class="contract-form-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <path d="M9 15l2 2 4-4"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.contractNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ form.contractNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="contractRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 合同基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('basic')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                  </svg>
                </span>
                合同基本信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.basic">
              <el-row>
                <el-col :span="8"><el-form-item label="合同编号" prop="contractNo"><el-input v-model="form.contractNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="合同名称" prop="contractName"><el-input v-model="form.contractName" placeholder="请输入合同名称" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="合同类型" prop="contractType">
                  <el-select v-model="form.contractType" placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in marketing_contract_type" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="8"><el-form-item label="关联客户" prop="customerId">
                  <el-select v-model="form.customerId" filterable placeholder="请选择客户" style="width: 100%" @change="onCustomerChange">
                    <el-option v-for="c in customerOptions" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="8"><el-form-item label="合同金额" prop="contractAmount"><el-input-number v-model="form.contractAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="付款方式" prop="paymentMethod">
                  <el-select v-model="form.paymentMethod" placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in marketing_payment_method" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
              <el-row>
                <el-col :span="24"><el-form-item label="合同附件" prop="attachment">
                  <file-upload v-model="form.attachment" :fileSize="50" />
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 日期信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('dates')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                    <line x1="16" y1="2" x2="16" y2="6"/>
                    <line x1="8" y1="2" x2="8" y2="6"/>
                    <line x1="3" y1="10" x2="21" y2="10"/>
                  </svg>
                </span>
                日期信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dates }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.dates">
              <el-row>
                <el-col :span="8"><el-form-item label="签约日期" prop="signDate"><el-date-picker v-model="form.signDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="生效日期" prop="effectiveDate"><el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="到期日期" prop="expireDate"><el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 负责信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('owner')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                  </svg>
                </span>
                负责信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.owner }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.owner">
              <el-row>
                <el-col :span="12"><el-form-item label="负责人" prop="userId">
                  <el-select v-model="form.userId" filterable clearable placeholder="请选择负责人" style="width: 100%" @change="onUserChange">
                    <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="所属部门" prop="deptId">
                  <el-tree-select v-model="form.deptId" :data="deptOptions" :props="{ value: 'id', label: 'label', children: 'children' }" value-key="id" placeholder="请选择部门" check-strictly clearable style="width: 100%" />
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 回款计划 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('paymentPlans')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="12" y1="1" x2="12" y2="23"/>
                    <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
                  </svg>
                </span>
                回款计划
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.paymentPlans }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.paymentPlans">
              <el-row :gutter="10" class="mb8">
                <el-col :span="1.5"><el-button type="primary" plain icon="Plus" size="small" @click="handleAddPlan">添加回款计划</el-button></el-col>
              </el-row>
              <el-table border :data="form.paymentPlanList" size="small">
                <el-table-column label="期次" width="80" align="center">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.periodNo" :min="1" :controls="false" size="small" style="width: 100%" />
                  </template>
                </el-table-column>
                <el-table-column label="计划回款金额" width="160" align="center">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.planAmount" :min="0" :precision="2" :controls="false" size="small" style="width: 100%" />
                  </template>
                </el-table-column>
                <el-table-column label="计划回款日期" width="180" align="center">
                  <template #default="scope">
                    <el-date-picker v-model="scope.row.planDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" size="small" style="width: 100%" />
                  </template>
                </el-table-column>
                <el-table-column label="实际回款金额" width="160" align="center">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.actualAmount" :min="0" :precision="2" :controls="false" size="small" style="width: 100%" />
                  </template>
                </el-table-column>
                <el-table-column label="实际回款日期" width="180" align="center">
                  <template #default="scope">
                    <el-date-picker v-model="scope.row.actualDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" size="small" style="width: 100%" />
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="120" align="center">
                  <template #default="scope"><dict-tag :options="marketing_payment_status" :value="scope.row.paymentStatus" /></template>
                </el-table-column>
                <el-table-column label="操作" width="80" align="center">
                  <template #default="scope">
                    <el-button link type="danger" icon="Delete" size="small" @click="handleDeletePlan(scope.$index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </section>

          <!-- 其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('other')">
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
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.other }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.other">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewOpen" width="1100px" append-to-body draggable class="contract-detail-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <path d="M9 15l2 2 4-4"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">合同详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.contractNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewForm.contractNo }}</span>
            <dict-tag :options="marketing_contract_status" :value="viewForm.contractStatus" />
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
              </span>
              基本信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ viewForm.contractNo }}</div></div>
              <div class="rd-item"><span class="rd-label">合同名称</span><div class="rd-value">{{ viewForm.contractName }}</div></div>
              <div class="rd-item"><span class="rd-label">合同类型</span><div class="rd-value"><dict-tag :options="marketing_contract_type" :value="viewForm.contractType" /></div></div>
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ viewForm.customerName }}</div></div>
              <div class="rd-item"><span class="rd-label">合同金额</span><div class="rd-value rd-value--large rd-amount">￥{{ formatAmount(viewForm.contractAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">付款方式</span><div class="rd-value"><dict-tag :options="marketing_payment_method" :value="viewForm.paymentMethod" /></div></div>
              <div class="rd-item"><span class="rd-label">合同状态</span><div class="rd-value"><dict-tag :options="marketing_contract_status" :value="viewForm.contractStatus" /></div></div>
            </div>
          </div>
        </section>

        <!-- 合同附件 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('attachment')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
                </svg>
              </span>
              合同附件
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.attachment }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.attachment">
            <template v-if="viewForm.attachment">
              <div class="rd-grid">
                <div class="rd-item rd-item--full" v-for="(file, idx) in viewForm.attachment.split(',')" :key="idx">
                  <span class="rd-label">附件{{ idx + 1 }}</span>
                  <div class="rd-value">
                    <el-link :href="baseUrl + file" :underline="false" target="_blank" type="primary"><el-icon><Download /></el-icon> {{ getFileName(file) }}</el-link>
                    <el-button link type="success" icon="View" size="small" style="margin-left: 12px" @click="handlePreview(file)">预览</el-button>
                  </div>
                </div>
              </div>
            </template>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg>
              <p class="rd-empty-text">暂无附件</p>
            </div>
          </div>
        </section>

        <!-- 日期信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dates')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </span>
              日期信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dates }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dates">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">签约日期</span><div class="rd-value">{{ viewForm.signDate }}</div></div>
              <div class="rd-item"><span class="rd-label">生效日期</span><div class="rd-value">{{ viewForm.effectiveDate }}</div></div>
              <div class="rd-item"><span class="rd-label">到期日期</span><div class="rd-value">{{ viewForm.expireDate }}</div></div>
            </div>
          </div>
        </section>

        <!-- 负责信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('owner')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
              </span>
              负责信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.owner }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.owner">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value">{{ viewForm.userName }}</div></div>
              <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ viewForm.deptName }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime }}</div></div>
            </div>
          </div>
        </section>

        <!-- 回款计划 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('paymentPlans')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="12" y1="1" x2="12" y2="23"/>
                  <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
                </svg>
              </span>
              回款计划
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.paymentPlans }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.paymentPlans">
            <el-table border :data="viewForm.paymentPlanList" size="small">
              <el-table-column label="期次" prop="periodNo" width="80" align="center" />
              <el-table-column label="计划回款金额" width="140" align="center"><template #default="scope">{{ formatAmount(scope.row.planAmount) }}</template></el-table-column>
              <el-table-column label="计划回款日期" prop="planDate" width="130" align="center" />
              <el-table-column label="实际回款金额" width="140" align="center"><template #default="scope">{{ formatAmount(scope.row.actualAmount) }}</template></el-table-column>
              <el-table-column label="实际回款日期" prop="actualDate" width="130" align="center" />
              <el-table-column label="状态" align="center">
                <template #default="scope"><dict-tag :options="marketing_payment_status" :value="scope.row.paymentStatus" /></template>
              </el-table-column>
            </el-table>
            <div class="rd-empty" v-if="!viewForm.paymentPlanList || viewForm.paymentPlanList.length === 0">
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
              <p class="rd-empty-text">暂无回款计划</p>
            </div>
          </div>
        </section>

        <!-- 审批信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('approve')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <path d="M9 15l2 2 4-4"/>
                </svg>
              </span>
              审批信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.approve }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.approve">
            <div class="rd-timeline" v-if="viewForm.approveLogList && viewForm.approveLogList.length">
              <div class="rd-timeline-item" v-for="(log, idx) in viewForm.approveLogList" :key="idx">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.actionType === '2', 'rd-timeline-dot--error': log.actionType === '3' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.actionType === '1'" type="primary" size="small" effect="light" round>提交审批</el-tag>
                      <el-tag v-else-if="log.actionType === '2'" type="success" size="small" effect="light" round>审批通过</el-tag>
                      <el-tag v-else-if="log.actionType === '3'" type="danger" size="small" effect="light" round>审批驳回</el-tag>
                      <el-tag v-else-if="log.actionType === '4'" type="warning" size="small" effect="light" round>合同终止</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.approveTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value">{{ log.approveBy }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.approveOpinion">
                    <strong>审批意见：</strong>{{ log.approveOpinion }}
                  </div>
                </div>
              </div>
            </div>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
              <p class="rd-empty-text">暂无审批记录</p>
            </div>
          </div>
        </section>

        <!-- 变更记录 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('changes')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
                  <path d="M3 3v5h5"/>
                </svg>
              </span>
              变更记录
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.changes }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.changes">
            <el-table border :data="viewForm.changeLogs" v-if="viewForm.changeLogs && viewForm.changeLogs.length > 0" size="small">
              <el-table-column label="变更类型" width="100" align="center">
                <template #default="scope"><dict-tag :options="marketing_contract_change_type" :value="scope.row.changeType" /></template>
              </el-table-column>
              <el-table-column label="变更字段" prop="fieldName" width="120" align="center" />
              <el-table-column label="原值" prop="oldValue" width="120" align="center" show-overflow-tooltip />
              <el-table-column label="新值" prop="newValue" width="120" align="center" show-overflow-tooltip />
              <el-table-column label="变更原因" prop="changeReason" align="center" show-overflow-tooltip />
              <el-table-column label="状态" width="80" align="center">
                <template #default="scope"><dict-tag :options="marketing_contract_change_status" :value="scope.row.changeStatus" /></template>
              </el-table-column>
              <el-table-column label="审批意见" prop="approveOpinion" width="150" align="center" show-overflow-tooltip />
            </el-table>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/></svg>
              <p class="rd-empty-text">暂无变更记录</p>
            </div>
          </div>
        </section>

        <!-- 其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('other')">
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
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.other }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.other">
            <div class="rd-grid">
              <div class="rd-item rd-item--full">
                <span class="rd-label">备注</span>
                <div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无备注' }}</div>
              </div>
            </div>
          </div>
        </section>
      </div>
      <!-- 变更审批模式：底部审批操作区 -->
      <template #footer v-if="viewApproveMode && pendingChangeLog">
        <div class="change-approve-footer">
          <div class="change-approve-info">
            <span class="change-approve-label">待审批变更：</span>
            <el-tag size="small" type="warning">{{ pendingChangeLog.fieldName }}</el-tag>
            <span class="change-approve-values">
              <span class="change-approve-old">{{ pendingChangeLog.oldValue }}</span>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: #6b7280;"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
              <span class="change-approve-new">{{ pendingChangeLog.newValue }}</span>
            </span>
          </div>
          <div class="change-approve-actions">
            <el-input v-model="changeApproveOpinion" placeholder="审批意见（可选）" style="width: 300px;" clearable />
            <el-button type="success" icon="Check" @click="confirmChangeApprove(true)">通过</el-button>
            <el-button type="danger" icon="Close" @click="confirmChangeApprove(false)">驳回</el-button>
            <el-button @click="viewOpen = false">取 消</el-button>
          </div>
        </div>
      </template>
      <template #footer v-else>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="changeOpen" width="900px" append-to-body draggable class="contract-change-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 20h9"/>
              <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">合同变更</span>
          <div class="rd-detail-header-sub" v-if="changeForm.contractNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ changeForm.contractNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 原合同信息（只读） -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('changeOriginal')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                </svg>
              </span>
              原合同信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.changeOriginal }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.changeOriginal">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ changeForm.contractNo }}</div></div>
              <div class="rd-item"><span class="rd-label">合同名称</span><div class="rd-value">{{ changeForm.contractName }}</div></div>
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ changeForm.customerName }}</div></div>
              <div class="rd-item"><span class="rd-label">合同金额</span><div class="rd-value rd-amount">￥{{ formatAmount(changeForm.originalContractAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">签约日期</span><div class="rd-value">{{ changeForm.originalSignDate }}</div></div>
              <div class="rd-item"><span class="rd-label">生效日期</span><div class="rd-value">{{ changeForm.originalEffectiveDate }}</div></div>
              <div class="rd-item"><span class="rd-label">到期日期</span><div class="rd-value">{{ changeForm.originalExpireDate }}</div></div>
              <div class="rd-item"><span class="rd-label">付款方式</span><div class="rd-value"><dict-tag :options="marketing_payment_method" :value="changeForm.originalPaymentMethod" /></div></div>
            </div>
          </div>
        </section>

        <!-- 变更内容 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('changeContent')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 20h9"/>
                  <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
                </svg>
              </span>
              变更内容
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.changeContent }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.changeContent">
            <el-form ref="changeRef" :model="changeForm" :rules="changeRules" label-width="110px">
              <el-row>
                <el-col :span="24">
                  <el-form-item label="变更类型" prop="changeType">
                    <el-radio-group v-model="changeForm.changeType" @change="onChangeTypeChange">
                      <el-radio-button v-for="d in marketing_contract_change_type" :key="d.value" :label="d.value">{{ d.label }}</el-radio-button>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 金额变更 -->
              <template v-if="changeForm.changeType === '1'">
                <el-row>
                  <el-col :span="12">
                    <el-form-item label="变更字段">
                      <el-select v-model="changeForm.fieldName" style="width: 100%" @change="onChangeFieldChange">
                        <el-option label="合同金额" value="contractAmount" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="原金额">
                      <el-input-number v-model="changeForm.oldAmount" :precision="2" :controls="false" disabled style="width: 100%" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="12">
                    <el-form-item label="新金额" prop="newAmount">
                      <el-input-number v-model="changeForm.newAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="差额">
                      <span class="change-diff" :class="{ 'change-diff--up': changeAmountDiff > 0, 'change-diff--down': changeAmountDiff < 0 }">
                        {{ changeAmountDiff > 0 ? '+' : '' }}￥{{ formatAmount(Math.abs(changeAmountDiff)) }}
                      </span>
                    </el-form-item>
                  </el-col>
                </el-row>
              </template>

              <!-- 日期变更 -->
              <template v-if="changeForm.changeType === '2'">
                <el-row>
                  <el-col :span="24">
                    <el-form-item label="变更字段" prop="fieldName">
                      <el-select v-model="changeForm.fieldName" style="width: 100%" @change="onChangeFieldChange">
                        <el-option label="签约日期" value="signDate" />
                        <el-option label="生效日期" value="effectiveDate" />
                        <el-option label="到期日期" value="expireDate" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="12">
                    <el-form-item label="原日期">
                      <el-date-picker v-model="changeForm.oldDateValue" type="date" value-format="YYYY-MM-DD" disabled style="width: 100%" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="新日期" prop="newDateValue">
                      <el-date-picker v-model="changeForm.newDateValue" type="date" value-format="YYYY-MM-DD" placeholder="选择新日期" style="width: 100%" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </template>

              <!-- 条款变更 -->
              <template v-if="changeForm.changeType === '3'">
                <el-row>
                  <el-col :span="24">
                    <el-form-item label="变更字段" prop="fieldName">
                      <el-select v-model="changeForm.fieldName" style="width: 100%" @change="onChangeFieldChange">
                        <el-option label="付款方式" value="paymentMethod" />
                        <el-option label="合同名称" value="contractName" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="12">
                    <el-form-item label="原值">
                      <el-input v-if="changeForm.fieldName === 'contractName'" v-model="changeForm.oldValue" disabled />
                      <el-select v-else v-model="changeForm.oldValue" disabled style="width: 100%">
                        <el-option v-for="d in marketing_payment_method" :key="d.value" :label="d.label" :value="d.value" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="新值" prop="newValue">
                      <el-input v-if="changeForm.fieldName === 'contractName'" v-model="changeForm.newValue" placeholder="请输入新值" />
                      <el-select v-else v-model="changeForm.newValue" placeholder="请选择" style="width: 100%">
                        <el-option v-for="d in marketing_payment_method" :key="d.value" :label="d.label" :value="d.value" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
              </template>

              <!-- 其他变更 -->
              <template v-if="changeForm.changeType === '4'">
                <el-row>
                  <el-col :span="24">
                    <el-form-item label="变更字段" prop="fieldName">
                      <el-input v-model="changeForm.fieldName" placeholder="请输入变更字段名称" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="12">
                    <el-form-item label="原值">
                      <el-input v-model="changeForm.oldValue" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="新值" prop="newValue">
                      <el-input v-model="changeForm.newValue" placeholder="请输入新值" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </template>

              <!-- 变更原因（所有类型通用） -->
              <el-row>
                <el-col :span="24">
                  <el-form-item label="变更原因" prop="changeReason">
                    <el-input v-model="changeForm.changeReason" type="textarea" :rows="3" placeholder="请详细说明变更原因，如：客户因业务调整需延期交付，申请将到期日期延长3个月等" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitChange">提交变更申请</el-button>
        <el-button @click="changeOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 终止合同对话框 -->
    <el-dialog title="终止合同" v-model="terminateOpen" width="500px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="合同编号">
          <span>{{ terminateForm.contractNo }}</span>
        </el-form-item>
        <el-form-item label="合同名称">
          <span>{{ terminateForm.contractName }}</span>
        </el-form-item>
        <el-form-item label="终止原因">
          <el-input v-model="terminateForm.terminateReason" type="textarea" :rows="3" placeholder="请输入终止原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="confirmTerminate">确认终止</el-button>
        <el-button @click="terminateOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 合同续签对话框 -->
    <el-dialog v-model="renewOpen" width="1100px" append-to-body draggable class="contract-renew-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8"/>
              <path d="M21 3v5h-5"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">合同续签</span>
          <div class="rd-detail-header-sub" v-if="renewForm.originalContractNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">原合同：{{ renewForm.originalContractNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 原合同信息（只读） -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('renewOriginal')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                </svg>
              </span>
              原合同信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.renewOriginal }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.renewOriginal">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ renewForm.originalContractNo }}</div></div>
              <div class="rd-item"><span class="rd-label">合同名称</span><div class="rd-value">{{ renewForm.originalContractName }}</div></div>
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ renewForm.originalCustomerName }}</div></div>
              <div class="rd-item"><span class="rd-label">合同金额</span><div class="rd-value rd-amount">￥{{ formatAmount(renewForm.originalAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">生效日期</span><div class="rd-value">{{ renewForm.originalEffectiveDate }}</div></div>
              <div class="rd-item"><span class="rd-label">到期日期</span><div class="rd-value" :class="{ 'rd-value--warning': isExpired }">{{ renewForm.originalExpireDate }}</div></div>
            </div>
          </div>
        </section>

        <!-- 续签条款（可编辑） -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('renewTerms')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 20h9"/>
                  <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
                </svg>
              </span>
              续签条款
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.renewTerms }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.renewTerms">
            <el-form ref="renewRef" :model="renewForm" :rules="renewRules" label-width="110px">
              <el-row>
                <el-col :span="12">
                  <el-form-item label="新合同名称" prop="newContractName">
                    <el-input v-model="renewForm.newContractName" placeholder="请输入新合同名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="合同金额" prop="newAmount">
                    <el-input-number v-model="renewForm.newAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="8">
                  <el-form-item label="生效日期" prop="newEffectiveDate">
                    <el-date-picker v-model="renewForm.newEffectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="到期日期" prop="newExpireDate">
                    <el-date-picker v-model="renewForm.newExpireDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="付款方式" prop="newPaymentMethod">
                    <el-select v-model="renewForm.newPaymentMethod" placeholder="请选择" style="width: 100%">
                      <el-option v-for="d in marketing_payment_method" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
        </section>

        <!-- 续签附件 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('renewAttachment')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
                </svg>
              </span>
              续签附件
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.renewAttachment }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.renewAttachment">
            <el-form label-width="110px">
              <el-form-item label="合同附件">
                <file-upload v-model="renewForm.attachment" :fileSize="50" />
              </el-form-item>
            </el-form>
          </div>
        </section>

        <!-- 续签说明 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('renewRemark')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="16" x2="12" y2="12"/>
                  <line x1="12" y1="8" x2="12.01" y2="8"/>
                </svg>
              </span>
              续签说明
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.renewRemark }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.renewRemark">
            <el-form label-width="110px">
              <el-form-item label="续签说明">
                <el-input v-model="renewForm.remark" type="textarea" :rows="3" placeholder="请输入续签说明/原因，如：原合同即将到期，双方协商续签一年等" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="primary" @click="confirmRenew">确认续签</el-button>
        <el-button @click="renewOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 合同审批对话框 -->
    <el-dialog v-model="approveOpen" width="900px" append-to-body draggable class="contract-approve-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <path d="M9 15l2 2 4-4"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">合同审批</span>
          <div class="rd-detail-header-sub" v-if="approveForm.contractNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ approveForm.contractNo }}</span>
            <dict-tag :options="marketing_contract_status" :value="approveForm.contractStatus" />
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('approveBasic')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
              </span>
              基本信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.approveBasic }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.approveBasic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ approveForm.contractNo }}</div></div>
              <div class="rd-item"><span class="rd-label">合同名称</span><div class="rd-value">{{ approveForm.contractName }}</div></div>
              <div class="rd-item"><span class="rd-label">合同类型</span><div class="rd-value"><dict-tag :options="marketing_contract_type" :value="approveForm.contractType" /></div></div>
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ approveForm.customerName }}</div></div>
              <div class="rd-item"><span class="rd-label">合同金额</span><div class="rd-value rd-value--large rd-amount">￥{{ formatAmount(approveForm.contractAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">付款方式</span><div class="rd-value"><dict-tag :options="marketing_payment_method" :value="approveForm.paymentMethod" /></div></div>
              <div class="rd-item"><span class="rd-label">合同状态</span><div class="rd-value"><dict-tag :options="marketing_contract_status" :value="approveForm.contractStatus" /></div></div>
            </div>
          </div>
        </section>

        <!-- 日期信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('approveDates')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </span>
              日期信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.approveDates }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.approveDates">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">签约日期</span><div class="rd-value">{{ approveForm.signDate }}</div></div>
              <div class="rd-item"><span class="rd-label">生效日期</span><div class="rd-value">{{ approveForm.effectiveDate }}</div></div>
              <div class="rd-item"><span class="rd-label">到期日期</span><div class="rd-value">{{ approveForm.expireDate }}</div></div>
            </div>
          </div>
        </section>

        <!-- 负责信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('approveOwner')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
              </span>
              负责信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.approveOwner }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.approveOwner">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value">{{ approveForm.userName }}</div></div>
              <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ approveForm.deptName }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ approveForm.createTime }}</div></div>
            </div>
          </div>
        </section>

        <!-- 审批意见 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('approveOpinion')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <path d="M9 15l2 2 4-4"/>
                </svg>
              </span>
              审批意见
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.approveOpinion }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.approveOpinion">
            <el-form label-width="100px">
              <el-form-item label="审批意见" required>
                <el-input v-model="approveForm.approveOpinion" type="textarea" :rows="4" placeholder="请输入审批意见" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="success" @click="confirmApprove(true)">通过</el-button>
        <el-button type="danger" @click="confirmApprove(false)">驳回</el-button>
        <el-button @click="approveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 文件预览组件 -->
    <file-preview ref="filePreviewRef" />
  </div>
</template>

<script setup name="MkContract">
import { listContract, getContract, addContract, updateContract, delContract, submitContract, approveContract, rejectContract, terminateContract, renewContract, submitContractChange, approveContractChange } from '@/api/mk/contract'
import { listCustomer } from '@/api/mk/customer'
import { listUser, deptTreeSelect } from '@/api/system/user'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_contract_index')
const { marketing_contract_type, marketing_contract_status, marketing_payment_method, marketing_payment_status, marketing_contract_change_type, marketing_contract_change_status } = proxy.useDict('marketing_contract_type', 'marketing_contract_status', 'marketing_payment_method', 'marketing_payment_status', 'marketing_contract_change_type', 'marketing_contract_change_status')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewApproveMode = ref(false)
const changeApproveOpinion = ref('')
const terminateOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const userOptions = ref([])
const deptOptions = ref([])
const customerOptions = ref([])
const viewForm = ref({})
const terminateForm = ref({})
const approveOpen = ref(false)
const approveForm = ref({})
const renewOpen = ref(false)
const renewForm = ref({})
const renewRules = {
  newContractName: [{ required: true, message: '请输入新合同名称', trigger: 'blur' }],
  newAmount: [{ required: true, message: '合同金额不能为空', trigger: 'blur' }],
  newEffectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }],
  newExpireDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }]
}
const isExpired = computed(() => {
  if (!renewForm.value.originalExpireDate) return false
  return new Date(renewForm.value.originalExpireDate) < new Date()
})
const collapsedCards = reactive({ basic: false, attachment: false, dates: false, owner: false, paymentPlans: false, approve: false, changes: false, other: false, approveBasic: false, approveDates: false, approveOwner: false, approveOpinion: false, renewOriginal: false, renewTerms: false, renewAttachment: false, renewRemark: false, changeOriginal: false, changeContent: false })
function toggleCard(name) { collapsedCards[name] = !collapsedCards[name] }
const baseUrl = import.meta.env.VITE_APP_BASE_API

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, contractNo: undefined, contractName: undefined, customerName: undefined, contractStatus: undefined },
rules: {
contractName: [{ required: true, message: '合同名称不能为空', trigger: 'blur' }],
    customerId: [{ required: true, message: '请选择关联客户', trigger: 'change' }],
    contractAmount: [{ required: true, message: '合同金额不能为空', trigger: 'blur' }],
    signDate: [{ required: true, message: '请选择签约日期', trigger: 'change' }],
    effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }],
    expireDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listContract(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function getUserOptions() {
  listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) })
}
function getDeptTree() {
  deptTreeSelect().then(res => { deptOptions.value = res.data })
}
function getCustomerOptions() {
  listCustomer({ pageNum: 1, pageSize: 9999 }).then(res => { customerOptions.value = res.rows })
}
function onUserChange(userId) {
  if (userId) {
    const user = userOptions.value.find(u => u.userId === userId)
    if (user) {
      form.value.userName = user.nickName
      if (user.deptId) {
        form.value.deptId = user.deptId
        form.value.deptName = user.dept ? user.dept.deptName : undefined
      }
    }
  } else {
    form.value.userName = undefined
  }
}
function onCustomerChange(customerId) {
  if (customerId) {
    const customer = customerOptions.value.find(c => c.customerId === customerId)
    if (customer) { form.value.customerName = customer.customerName }
  } else {
    form.value.customerName = undefined
  }
}
function handleAddPlan() {
  if (!form.value.paymentPlanList) { form.value.paymentPlanList = [] }
  form.value.paymentPlanList.push({ periodNo: form.value.paymentPlanList.length + 1, planAmount: 0, planDate: undefined, actualAmount: 0, actualDate: undefined, paymentStatus: '0' })
}
function handleDeletePlan(index) {
  form.value.paymentPlanList.splice(index, 1)
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.contractId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = { contractNo: undefined, contractName: undefined, contractType: '0', customerId: undefined, customerName: undefined, opportunityId: undefined, contractAmount: 0, signDate: undefined, effectiveDate: undefined, expireDate: undefined, paymentMethod: '0', contractStatus: '0', userId: undefined, userName: undefined, deptId: undefined, deptName: undefined, attachment: undefined, paymentPlanList: [], remark: undefined }
  proxy.resetForm('contractRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增合同' }
function handleUpdate(row) {
  reset()
  getContract(row.contractId || ids.value[0]).then(res => {
    if (res.data.contractStatus !== '0') {
      proxy.$modal.msgWarning('非草稿状态的合同不允许修改')
      return
    }
    form.value = res.data
    if (!form.value.paymentPlanList) { form.value.paymentPlanList = [] }
    open.value = true
    title.value = '修改合同'
  })
}
function formatAmount(val) { if (val == null || val === '') return '-'; return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }
function handleView(row, approveMode = false) {
  getContract(row.contractId).then(res => {
    viewForm.value = res.data
    collapsedCards.attachment = !res.data.attachment
    collapsedCards.paymentPlans = !res.data.paymentPlanList || res.data.paymentPlanList.length === 0
    collapsedCards.approve = !res.data.approveLogList || res.data.approveLogList.length === 0
    collapsedCards.changes = !res.data.changeLogs || res.data.changeLogs.length === 0
    collapsedCards.other = !res.data.remark
    viewApproveMode.value = approveMode
    changeApproveOpinion.value = ''
    viewOpen.value = true
  })
}

// 待审批的变更记录（用于详情页审批模式）
const pendingChangeLog = computed(() => {
  if (!viewForm.value.changeLogs) return null
  return viewForm.value.changeLogs.find(log => log.changeStatus === '0') || null
})

// 在详情页直接审批变更
function confirmChangeApprove(approved) {
  const log = pendingChangeLog.value
  if (!log) return
  const msg = approved ? '通过' : '驳回'
  approveContractChange(log.logId, approved, changeApproveOpinion.value).then(() => {
    proxy.$modal.msgSuccess(msg + '成功')
    viewOpen.value = false
    getList()
  })
}
function submitForm() {
  proxy.$refs['contractRef'].validate(valid => {
    if (valid) {
      if (form.value.contractId != undefined) { updateContract(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addContract(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function getFileName(url) {
  if (url.lastIndexOf('/') > -1) {
    return url.slice(url.lastIndexOf('/') + 1)
  }
  return url
}
function handlePreview(fileUrl) {
  proxy.$refs.filePreviewRef.open(fileUrl, getFileName(fileUrl))
}
function handleDelete(row) {
  const contractIds = row.contractId || ids.value
  proxy.$modal.confirm('确认删除编号为"' + contractIds + '"的数据？').then(() => delContract(contractIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleSubmit(row) {
  proxy.$modal.confirm('确认提交合同【' + row.contractNo + '】进行审批？').then(() => submitContract(row.contractId)).then(() => { getList(); proxy.$modal.msgSuccess('已提交审批') }).catch(() => {})
}
function handleApprove(row) {
  getContract(row.contractId).then(res => {
    approveForm.value = { ...res.data, approveOpinion: '' }
    approveOpen.value = true
  })
}
function confirmApprove(passed) {
  if (!approveForm.value.approveOpinion) {
    proxy.$modal.msgWarning('请输入审批意见')
    return
  }
  if (passed) {
    approveContract(approveForm.value.contractId, approveForm.value.approveOpinion).then(() => {
      proxy.$modal.msgSuccess('审批通过，合同已生效')
      approveOpen.value = false
      getList()
    })
  } else {
    rejectContract(approveForm.value.contractId, approveForm.value.approveOpinion).then(() => {
      proxy.$modal.msgSuccess('已驳回，合同退回草稿状态')
      approveOpen.value = false
      getList()
    })
  }
}
function handleTerminate(row) {
  terminateForm.value = { contractId: row.contractId, contractNo: row.contractNo, contractName: row.contractName, terminateReason: '' }
  terminateOpen.value = true
}
function confirmTerminate() {
  terminateContract(terminateForm.value.contractId, terminateForm.value.terminateReason).then(() => {
    proxy.$modal.msgSuccess('合同已终止')
    terminateOpen.value = false
    getList()
  })
}

function handleRenew(row) {
  getContract(row.contractId).then(res => {
    const orig = res.data
    // 计算默认到期日期：原到期日期 + 1 年
    const origExpire = orig.expireDate ? new Date(orig.expireDate) : new Date()
    const defaultExpire = new Date(origExpire.getFullYear() + 1, origExpire.getMonth(), origExpire.getDate())
    // 默认生效日期：原到期日期（无缝衔接）
    const defaultEffective = orig.expireDate || proxy.parseTime(new Date(), '{y}-{m}-{d}')
    renewForm.value = {
      contractId: orig.contractId,
      // 原合同信息（只读展示）
      originalContractNo: orig.contractNo,
      originalContractName: orig.contractName,
      originalCustomerName: orig.customerName,
      originalAmount: orig.contractAmount,
      originalEffectiveDate: orig.effectiveDate,
      originalExpireDate: orig.expireDate,
      // 续签条款（可编辑，预填默认值）
      newContractName: orig.contractName + '(续签)',
      newAmount: orig.contractAmount,
      newEffectiveDate: defaultEffective,
      newExpireDate: proxy.parseTime(defaultExpire, '{y}-{m}-{d}'),
      newPaymentMethod: orig.paymentMethod,
      // 续签附件
      attachment: undefined,
      // 续签说明
      remark: ''
    }
    renewOpen.value = true
  })
}

function confirmRenew() {
  proxy.$refs['renewRef'].validate(valid => {
    if (!valid) return
    // 校验到期日期必须晚于生效日期
    if (renewForm.value.newExpireDate <= renewForm.value.newEffectiveDate) {
      proxy.$modal.msgWarning('到期日期必须晚于生效日期')
      return
    }
    const submitData = {
      newContractName: renewForm.value.newContractName,
      newAmount: renewForm.value.newAmount,
      newEffectiveDate: renewForm.value.newEffectiveDate,
      newExpireDate: renewForm.value.newExpireDate,
      newPaymentMethod: renewForm.value.newPaymentMethod,
      attachment: renewForm.value.attachment,
      remark: renewForm.value.remark
    }
    renewContract(renewForm.value.contractId, submitData).then(() => {
      proxy.$modal.msgSuccess('续签合同已创建，请完善信息后提交审批')
      renewOpen.value = false
      getList()
    })
  })
}

const changeOpen = ref(false)
const changeForm = ref({})
const changeRules = {
  changeType: [{ required: true, message: '请选择变更类型', trigger: 'change' }],
  fieldName: [{ required: true, message: '请选择变更字段', trigger: 'change' }],
  newAmount: [{ required: true, message: '请输入新金额', trigger: 'blur' }],
  newDateValue: [{ required: true, message: '请选择新日期', trigger: 'change' }],
  newValue: [{ required: true, message: '请输入新值', trigger: 'blur' }],
  changeReason: [{ required: true, message: '请输入变更原因', trigger: 'blur' }]
}
const changeAmountDiff = computed(() => {
  if (changeForm.value.newAmount == null || changeForm.value.oldAmount == null) return 0
  return Number(changeForm.value.newAmount) - Number(changeForm.value.oldAmount)
})

// 变更字段标签映射
const changeFieldLabels = {
  contractAmount: '合同金额',
  signDate: '签约日期',
  effectiveDate: '生效日期',
  expireDate: '到期日期',
  paymentMethod: '付款方式',
  contractName: '合同名称'
}

function handleChange(row) {
  getContract(row.contractId).then(res => {
    const orig = res.data
    changeForm.value = {
      contractId: orig.contractId,
      contractNo: orig.contractNo,
      contractName: orig.contractName,
      customerName: orig.customerName,
      // 原始值（用于展示和提交）
      originalContractAmount: orig.contractAmount,
      originalSignDate: orig.signDate,
      originalEffectiveDate: orig.effectiveDate,
      originalExpireDate: orig.expireDate,
      originalPaymentMethod: orig.paymentMethod,
      // 变更表单
      changeType: '1',
      fieldName: 'contractAmount',
      // 金额变更专用
      oldAmount: orig.contractAmount,
      newAmount: orig.contractAmount,
      // 日期变更专用
      oldDateValue: orig.signDate,
      newDateValue: undefined,
      // 通用（条款变更/其他变更）
      oldValue: String(orig.contractAmount),
      newValue: String(orig.contractAmount),
      changeReason: ''
    }
    changeOpen.value = true
  })
}

function onChangeTypeChange() {
  const orig = changeForm.value
  if (changeForm.value.changeType === '1') {
    changeForm.value.fieldName = 'contractAmount'
    changeForm.value.oldAmount = orig.originalContractAmount
    changeForm.value.newAmount = orig.originalContractAmount
    changeForm.value.oldValue = String(orig.originalContractAmount)
    changeForm.value.newValue = String(orig.originalContractAmount)
  } else if (changeForm.value.changeType === '2') {
    changeForm.value.fieldName = 'signDate'
    changeForm.value.oldDateValue = orig.originalSignDate
    changeForm.value.newDateValue = undefined
    changeForm.value.oldValue = orig.originalSignDate || ''
    changeForm.value.newValue = ''
  } else if (changeForm.value.changeType === '3') {
    changeForm.value.fieldName = 'paymentMethod'
    changeForm.value.oldValue = orig.originalPaymentMethod || ''
    changeForm.value.newValue = orig.originalPaymentMethod || ''
  } else if (changeForm.value.changeType === '4') {
    changeForm.value.fieldName = ''
    changeForm.value.oldValue = ''
    changeForm.value.newValue = ''
  }
}

function onChangeFieldChange() {
  const orig = changeForm.value
  if (changeForm.value.changeType === '2') {
    // 日期变更：根据选择的字段更新原日期
    if (changeForm.value.fieldName === 'signDate') {
      changeForm.value.oldDateValue = orig.originalSignDate
      changeForm.value.oldValue = orig.originalSignDate || ''
    } else if (changeForm.value.fieldName === 'effectiveDate') {
      changeForm.value.oldDateValue = orig.originalEffectiveDate
      changeForm.value.oldValue = orig.originalEffectiveDate || ''
    } else if (changeForm.value.fieldName === 'expireDate') {
      changeForm.value.oldDateValue = orig.originalExpireDate
      changeForm.value.oldValue = orig.originalExpireDate || ''
    }
    changeForm.value.newDateValue = undefined
    changeForm.value.newValue = ''
  } else if (changeForm.value.changeType === '3') {
    // 条款变更：根据选择的字段更新原值
    if (changeForm.value.fieldName === 'paymentMethod') {
      changeForm.value.oldValue = orig.originalPaymentMethod || ''
      changeForm.value.newValue = orig.originalPaymentMethod || ''
    } else if (changeForm.value.fieldName === 'contractName') {
      changeForm.value.oldValue = orig.contractName || ''
      changeForm.value.newValue = orig.contractName || ''
    }
  }
}

function submitChange() {
  proxy.$refs['changeRef'].validate(valid => {
    if (!valid) return
    // 根据变更类型组装提交数据
    const f = changeForm.value
    let oldValueStr = ''
    let newValueStr = ''
    if (f.changeType === '1') {
      oldValueStr = String(f.oldAmount)
      newValueStr = String(f.newAmount)
    } else if (f.changeType === '2') {
      oldValueStr = f.oldDateValue || ''
      newValueStr = f.newDateValue || ''
    } else {
      oldValueStr = f.oldValue || ''
      newValueStr = f.newValue || ''
    }
    // 校验新值不能与原值相同
    if (oldValueStr === newValueStr) {
      proxy.$modal.msgWarning('新值不能与原值相同')
      return
    }
    const submitData = {
      contractId: f.contractId,
      changeType: f.changeType,
      fieldName: f.changeType === '4' ? f.fieldName : (changeFieldLabels[f.fieldName] || f.fieldName),
      oldValue: oldValueStr,
      newValue: newValueStr,
      changeReason: f.changeReason
    }
    submitContractChange(submitData).then(() => {
      proxy.$modal.msgSuccess('变更申请已提交，等待审批')
      changeOpen.value = false
      getList()
    })
  })
}

function handleExport() { proxy.download('mk/contract/export', { ...queryParams.value }, `contract_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
getUserOptions()
getDeptTree()
getCustomerOptions()
getList()
</script>

<style scoped>
.attachment-link { line-height: 28px; }

/* ===== 详情页卡片式样式（遵循 detail-page-style-guide 规范） ===== */
:deep(.contract-approve-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.contract-approve-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.contract-approve-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.contract-approve-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.contract-approve-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

:deep(.contract-form-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.contract-form-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.contract-form-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.contract-form-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.contract-form-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

:deep(.contract-detail-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.contract-detail-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.contract-detail-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.contract-detail-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.contract-detail-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

:deep(.contract-renew-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.contract-renew-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.contract-renew-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.contract-renew-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.contract-renew-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

:deep(.contract-change-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.contract-change-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.contract-change-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.contract-change-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.contract-change-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

:deep(.contract-changelog-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.contract-changelog-dialog .el-dialog__headerbtn) { top: 10px; right: 12px; z-index: 10; }
:deep(.contract-changelog-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.contract-changelog-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color: #fff; }
:deep(.contract-changelog-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }

.change-diff { font-size: 16px; font-weight: 700; font-variant-numeric: tabular-nums; }
.change-diff--up { color: #ef4444; }
.change-diff--down { color: #10b981; }

.rd-detail-header { display: flex; align-items: center; gap: 10px; padding: 8px 16px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -25px; right: -10px; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.12) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 8px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 2px 8px rgb(0 0 0 / 0.1); }
.rd-detail-header-title { font-size: 16px; font-weight: 700; color: #fff; letter-spacing: -0.02em; white-space: nowrap; }
.rd-detail-header-sub { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.rd-detail-header-divider { width: 1px; height: 16px; background: rgb(255 255 255 / 0.3); flex-shrink: 0; }
.rd-detail-header-no { font-size: 12px; font-weight: 500; color: rgb(255 255 255 / 0.85); font-variant-numeric: tabular-nums; white-space: nowrap; }

.rd-page { max-width: 1040px; margin: 0 auto; }

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
.rd-label-dot { display: none; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.rd-value--large { font-size: 18px; font-weight: 700; }
.rd-value--muted { color: #9ca3af; font-style: italic; }
.rd-value--warning { color: #f59e0b; font-weight: 600; }
.rd-amount { font-variant-numeric: tabular-nums; font-weight: 700; color: #111827; }

.rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border-radius: 6px; border: none; background: transparent; color: #9ca3af; cursor: pointer; transition: all 0.15s ease; flex-shrink: 0; }
.rd-collapse-btn:hover { background: #f3f4f6; color: #4b5563; }
.rd-collapse-btn svg { transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.rd-collapse-btn.is-collapsed svg { transform: rotate(-90deg); }

.rd-timeline { position: relative; padding-left: 24px; }
.rd-timeline::before { content: ''; position: absolute; left: 10px; top: 8px; bottom: 8px; width: 2px; background: linear-gradient(to bottom, #d1d5db, #e5e7eb); border-radius: 1px; }
.rd-timeline-item { position: relative; padding-bottom: 10px; }
.rd-timeline-item:last-child { padding-bottom: 0; }
.rd-timeline-dot { position: absolute; left: -24px; top: 4px; width: 20px; height: 20px; border-radius: 50%; background: #fff; border: 3px solid #9ca3af; display: flex; align-items: center; justify-content: center; z-index: 1; }
.rd-timeline-dot::after { content: ''; width: 6px; height: 6px; border-radius: 50%; background: #9ca3af; }
.rd-timeline-dot--success { border-color: #10b981; box-shadow: 0 0 0 4px #ecfdf5; }
.rd-timeline-dot--success::after { background: #10b981; }
.rd-timeline-dot--error { border-color: #ef4444; box-shadow: 0 0 0 4px #fef2f2; }
.rd-timeline-dot--error::after { background: #ef4444; }
.rd-timeline-content { background: #f9fafb; border-radius: 10px; padding: 10px; border: 1px solid #f3f4f6; }
.rd-timeline-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.rd-timeline-title { font-weight: 600; color: #111827; font-size: 14px; }
.rd-timeline-time { font-size: 12px; color: #6b7280; }
.rd-timeline-body { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px 20px; }
.rd-timeline-comment { margin-top: 6px; padding: 10px; background: #fff; border-radius: 8px; border-left: 3px solid #f59e0b; font-size: 14px; color: #374151; line-height: 1.6; }

.rd-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 20px; text-align: center; color: #9ca3af; background: #f9fafb; border-radius: 12px; border: 1px dashed #e5e7eb; }
.rd-empty-icon { margin-bottom: 4px; color: #d1d5db; }
.rd-empty-text { font-size: 14px; font-weight: 500; margin: 0; }

@keyframes rdFadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.rd-card:nth-child(2) { animation-delay: 0.06s; }
.rd-card:nth-child(3) { animation-delay: 0.12s; }
.rd-card:nth-child(4) { animation-delay: 0.18s; }

/* 变更审批底部操作区 */
.change-approve-footer { display: flex; flex-direction: column; gap: 12px; padding: 16px 20px; background: #fff; border-top: 1px solid #e5e7eb; }
.change-approve-info { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; font-size: 14px; color: #374151; }
.change-approve-label { font-weight: 600; color: #6b7280; white-space: nowrap; }
.change-approve-values { display: flex; align-items: center; gap: 6px; }
.change-approve-old { color: #9ca3af; text-decoration: line-through; font-variant-numeric: tabular-nums; }
.change-approve-new { color: #111827; font-weight: 700; font-variant-numeric: tabular-nums; }
.change-approve-actions { display: flex; align-items: center; gap: 8px; justify-content: flex-end; }

@media (max-width: 768px) { .rd-grid, .rd-timeline-body { grid-template-columns: 1fr; } .rd-card-header { padding: 8px 12px; } .rd-card-body { padding: 12px; } .change-approve-footer { padding: 12px; } .change-approve-actions { flex-wrap: wrap; } }
</style>
