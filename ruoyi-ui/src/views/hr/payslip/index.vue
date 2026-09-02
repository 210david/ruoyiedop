<template>
  <div class="app-container hr-payslip-page">
    <!-- 筛选条件卡片 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>员工姓名</label>
          <div class="control">
            <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工号</label>
          <div class="control">
            <el-input v-model="queryParams.employeeNo" placeholder="请输入工号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>发放状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.calcStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option label="草稿" value="0" /><el-option label="已锁定" value="1" /><el-option label="已发放" value="2" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>工资月份</label>
          <div class="control">
            <el-date-picker v-model="yearMonthRange" type="monthrange" range-separator="-" start-placeholder="开始月份" end-placeholder="结束月份" value-format="YYYY-MM" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>银行回单号</label>
          <div class="control">
            <el-input v-model="queryParams.bankReceiptNo" placeholder="请输入银行回单号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>发放日期</label>
          <div class="control">
            <el-date-picker v-model="payDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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

    <!-- 数据列表卡片 -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:payslip:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:payslip:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:payslip:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:payslip:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_payslip_index"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrPayslipList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" :width="colWidth('序号', 85)" align="center" />
          <el-table-column label="工资年月" prop="yearMonth" key="yearMonth" align="center" :width="colWidth('yearMonth', 110)" resizable v-if="columns.yearMonth.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.yearMonth }}</span></template>
          </el-table-column>
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 120)" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo }}</span></template>
          </el-table-column>
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :width="colWidth('employeeName', 100)" resizable v-if="columns.employeeName.visible" />
          <el-table-column label="基本工资" prop="basicSalary" key="basicSalary" align="center" :width="colWidth('basicSalary', 130)" resizable v-if="columns.basicSalary.visible" />
          <el-table-column label="岗位工资" prop="positionSalary" key="positionSalary" align="center" :width="colWidth('positionSalary', 130)" resizable v-if="columns.positionSalary.visible" />
          <el-table-column label="技能津贴" prop="skillAllowance" key="skillAllowance" align="center" :width="colWidth('skillAllowance', 120)" resizable v-if="columns.skillAllowance.visible" />
          <el-table-column label="应发合计" prop="grossSalary" key="grossSalary" align="center" :width="colWidth('grossSalary', 140)" resizable v-if="columns.grossSalary.visible" />
          <el-table-column label="实发工资" prop="netSalary" key="netSalary" align="center" :width="colWidth('netSalary', 140)" resizable v-if="columns.netSalary.visible" />
          <el-table-column label="发放状态" prop="calcStatus" key="calcStatus" align="center" :width="colWidth('calcStatus', 110)" resizable v-if="columns.calcStatus.visible">
            <template #default="scope">
              <span class="badge" :class="calcBadgeClass(scope.row.calcStatus)" v-if="scope.row.calcStatus !== undefined && scope.row.calcStatus !== null && scope.row.calcStatus !== ''">
                <span class="dot"></span>{{ calcLabel(scope.row.calcStatus) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="发放日期" prop="payDate" key="payDate" align="center" :width="colWidth('payDate', 130)" resizable v-if="columns.payDate.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:payslip:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:payslip:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:payslip:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrPayslipRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 卡片：基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="年月" prop="yearMonth"><el-input v-model="form.yearMonth" placeholder="请输入年月" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="姓名" prop="employeeId">
                  <el-input :model-value="form.employeeName" readonly placeholder="请选择人员">
                    <template #append>
                      <el-button v-if="form.employeeId" icon="CircleClose" @click="onEmployeeFieldClick" />
                      <el-button v-else icon="Search" @click="onEmployeeFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工号" prop="employeeNo"><el-input v-model="form.employeeNo" placeholder="请输入工号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="部门" prop="orgId">
                  <el-input :model-value="form.orgName" readonly placeholder="请选择部门">
                    <template #append>
                      <el-button v-if="form.orgId" icon="CircleClose" @click="onOrgFieldClick" />
                      <el-button v-else icon="Search" @click="onOrgFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 卡片：应发明细 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>应发明细</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="基本工资" prop="basicSalary"><el-input-number v-model="form.basicSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="岗位工资" prop="positionSalary"><el-input-number v-model="form.positionSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="技能津贴" prop="skillAllowance"><el-input-number v-model="form.skillAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工龄工资" prop="seniorityPay"><el-input-number v-model="form.seniorityPay" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="夜班津贴" prop="nightAllowance"><el-input-number v-model="form.nightAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="高温津贴" prop="highTempAllowance"><el-input-number v-model="form.highTempAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="有毒有害津贴" prop="hazardAllowance"><el-input-number v-model="form.hazardAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="餐补" prop="mealAllowance"><el-input-number v-model="form.mealAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="交通补贴" prop="transportAllowance"><el-input-number v-model="form.transportAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="全勤奖" prop="fullAttendanceBonus"><el-input-number v-model="form.fullAttendanceBonus" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计件工资" prop="pieceworkPay"><el-input-number v-model="form.pieceworkPay" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="加班费" prop="overtimePay"><el-input-number v-model="form.overtimePay" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="绩效奖金" prop="performanceBonus"><el-input-number v-model="form.performanceBonus" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="其他加项" prop="otherAdd"><el-input-number v-model="form.otherAdd" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 卡片：应扣明细 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="8" y1="12" x2="16" y2="12"/></svg></span>应扣明细</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="缺勤扣款" prop="absenceDeduction"><el-input-number v-model="form.absenceDeduction" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="其他扣项" prop="otherDeduct"><el-input-number v-model="form.otherDeduct" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="社保个人" prop="socialInsurance"><el-input-number v-model="form.socialInsurance" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="公积金个人" prop="houseFund"><el-input-number v-model="form.houseFund" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="个人所得税" prop="incomeTax"><el-input-number v-model="form.incomeTax" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="其他扣款" prop="otherDeduction"><el-input-number v-model="form.otherDeduction" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 卡片：汇总与发放 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/></svg></span>汇总与发放</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e3">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="应发合计" prop="grossSalary"><el-input-number v-model="form.grossSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="实发工资" prop="netSalary"><el-input-number v-model="form.netSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="状态" prop="calcStatus"><el-select v-model="form.calcStatus" placeholder="请选择" style="width: 100%"><el-option label="草稿" value="0" /><el-option label="已锁定" value="1" /><el-option label="已发放" value="2" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="发放日期" prop="payDate"><el-date-picker v-model="form.payDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="锁定人" prop="sealedBy"><el-input v-model="form.sealedBy" placeholder="请输入锁定人" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="锁定时间" prop="sealedTime"><el-date-picker v-model="form.sealedTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="银行回单号" prop="bankReceiptNo"><el-input v-model="form.bankReceiptNo" placeholder="请输入银行回单号" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">工资条详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.yearMonth">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">工资年月：{{ viewData.yearMonth }}</span>
              <span class="rd-detail-header-no" v-if="viewData.employeeName">姓名：{{ viewData.employeeName }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">工资年月</span><div class="rd-value">{{ viewData.yearMonth || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>应发明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">基本工资</span><div class="rd-value rd-amount">{{ formatAmount(viewData.basicSalary) }}</div></div>
              <div class="rd-item"><span class="rd-label">岗位工资</span><div class="rd-value rd-amount">{{ formatAmount(viewData.positionSalary) }}</div></div>
              <div class="rd-item"><span class="rd-label">技能津贴</span><div class="rd-value rd-amount">{{ formatAmount(viewData.skillAllowance) }}</div></div>
              <div class="rd-item"><span class="rd-label">工龄工资</span><div class="rd-value rd-amount">{{ formatAmount(viewData.seniorityPay) }}</div></div>
              <div class="rd-item"><span class="rd-label">夜班津贴</span><div class="rd-value rd-amount">{{ formatAmount(viewData.nightAllowance) }}</div></div>
              <div class="rd-item"><span class="rd-label">高温津贴</span><div class="rd-value rd-amount">{{ formatAmount(viewData.highTempAllowance) }}</div></div>
              <div class="rd-item"><span class="rd-label">有毒有害津贴</span><div class="rd-value rd-amount">{{ formatAmount(viewData.hazardAllowance) }}</div></div>
              <div class="rd-item"><span class="rd-label">餐补</span><div class="rd-value rd-amount">{{ formatAmount(viewData.mealAllowance) }}</div></div>
              <div class="rd-item"><span class="rd-label">交通补贴</span><div class="rd-value rd-amount">{{ formatAmount(viewData.transportAllowance) }}</div></div>
              <div class="rd-item"><span class="rd-label">全勤奖</span><div class="rd-value rd-amount">{{ formatAmount(viewData.fullAttendanceBonus) }}</div></div>
              <div class="rd-item"><span class="rd-label">计件工资</span><div class="rd-value rd-amount">{{ formatAmount(viewData.pieceworkPay) }}</div></div>
              <div class="rd-item"><span class="rd-label">加班费</span><div class="rd-value rd-amount">{{ formatAmount(viewData.overtimePay) }}</div></div>
              <div class="rd-item"><span class="rd-label">绩效奖金</span><div class="rd-value rd-amount">{{ formatAmount(viewData.performanceBonus) }}</div></div>
              <div class="rd-item"><span class="rd-label">其他加项</span><div class="rd-value rd-amount">{{ formatAmount(viewData.otherAdd) }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>应扣明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">缺勤扣款</span><div class="rd-value rd-amount">{{ formatAmount(viewData.absenceDeduction) }}</div></div>
              <div class="rd-item"><span class="rd-label">其他扣项</span><div class="rd-value rd-amount">{{ formatAmount(viewData.otherDeduct) }}</div></div>
              <div class="rd-item"><span class="rd-label">社保个人</span><div class="rd-value rd-amount">{{ formatAmount(viewData.socialInsurance) }}</div></div>
              <div class="rd-item"><span class="rd-label">公积金个人</span><div class="rd-value rd-amount">{{ formatAmount(viewData.houseFund) }}</div></div>
              <div class="rd-item"><span class="rd-label">个人所得税</span><div class="rd-value rd-amount">{{ formatAmount(viewData.incomeTax) }}</div></div>
              <div class="rd-item"><span class="rd-label">其他扣款</span><div class="rd-value rd-amount">{{ formatAmount(viewData.otherDeduction) }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>汇总与发放</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">应发合计</span><div class="rd-value rd-value--large rd-amount">{{ formatAmount(viewData.grossSalary) }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">实发工资</span><div class="rd-value rd-value--large rd-amount">{{ formatAmount(viewData.netSalary) }}</div></div>
              <div class="rd-item"><span class="rd-label">发放状态</span><div class="rd-value"><dict-tag :options="calcStatusOptions" :value="viewData.calcStatus" /></div></div>
              <div class="rd-item"><span class="rd-label">发放日期</span><div class="rd-value">{{ viewData.payDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">锁定人</span><div class="rd-value">{{ viewData.sealedBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">锁定时间</span><div class="rd-value">{{ viewData.sealedTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">银行回单号</span><div class="rd-value">{{ viewData.bankReceiptNo || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 人员选择弹窗 ===== -->
    <employee-picker ref="employeePickerRef" title="选择人员" @confirm="onEmployeePickerConfirm" />

    <!-- ===== 部门选择弹窗 ===== -->
    <dept-picker ref="orgPickerRef" title="选择部门" @confirm="onOrgPickerConfirm" />
  </div>
</template>

<script setup name="HrPayslip">
import { listHrPayslip, getHrPayslip, addHrPayslip, updateHrPayslip, delHrPayslip } from '@/api/hr/hrPayslip'
import { Filter, ArrowDown, Search, Edit, Delete, Download } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'
import EmployeePicker from '@/components/EmployeePicker'
import DeptPicker from '@/components/DeptPicker'

const { proxy } = getCurrentInstance()

const hrPayslipList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const showAdvanced = ref(false)
const yearMonthRange = ref([])
const payDateRange = ref([])
const collapsedCards = reactive({ c0: false, c1: false, c2: false, c3: false })

/** 发放状态字面映射 */
const calcStatusOptions = [
  { label: '草稿', value: '0' },
  { label: '已锁定', value: '1' },
  { label: '已发放', value: '2' }
]

/** 列显隐与列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_payslip_index')

const defaultColumns = {
  yearMonth: { label: '工资年月', visible: true },
  employeeNo: { label: '工号', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  basicSalary: { label: '基本工资', visible: true },
  positionSalary: { label: '岗位工资', visible: true },
  skillAllowance: { label: '技能津贴', visible: true },
  grossSalary: { label: '应发合计', visible: true },
  netSalary: { label: '实发工资', visible: true },
  calcStatus: { label: '发放状态', visible: true },
  payDate: { label: '发放日期', visible: true },
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_payslip_index')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, employeeName: undefined, employeeNo: undefined, calcStatus: undefined, bankReceiptNo: undefined },
  rules: {
    yearMonth: [{ required: true, message: '年月不能为空', trigger: 'blur' }],
    employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.employeeName) count++
  if (queryParams.value.employeeNo) count++
  if (queryParams.value.calcStatus) count++
  if (yearMonthRange.value && yearMonthRange.value.length === 2) count++
  if (queryParams.value.bankReceiptNo) count++
  if (payDateRange.value && payDateRange.value.length === 2) count++
  return count
})

const viewData = ref({})

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function calcLabel(v) {
  const d = calcStatusOptions.find(i => i.value == v)
  return d ? d.label : String(v)
}
function calcBadgeClass(v) {
  const map = { '0': 'amber', '1': 'blue', '2': 'green' }
  return map[v] || 'gray'
}

function formatAmount(val) {
  if (val === null || val === undefined || val === '') return '-'
  return '￥' + Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getList() {
  loading.value = true
  listHrPayslip(queryParams.value).then(response => {
    hrPayslipList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, yearMonthRange.value, 'YearMonth')
  proxy.addDateRange(queryParams.value, payDateRange.value, 'PayDate')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  queryParams.value.calcStatus = undefined
  queryParams.value.bankReceiptNo = undefined
  yearMonthRange.value = []
  payDateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.payslipId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    yearMonth: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    orgId: undefined,
    orgName: undefined,
    basicSalary: undefined,
    positionSalary: undefined,
    skillAllowance: undefined,
    seniorityPay: undefined,
    nightAllowance: undefined,
    highTempAllowance: undefined,
    hazardAllowance: undefined,
    mealAllowance: undefined,
    transportAllowance: undefined,
    fullAttendanceBonus: undefined,
    pieceworkPay: undefined,
    overtimePay: undefined,
    performanceBonus: undefined,
    absenceDeduction: undefined,
    otherAdd: undefined,
    otherDeduct: undefined,
    grossSalary: undefined,
    socialInsurance: undefined,
    houseFund: undefined,
    incomeTax: undefined,
    otherDeduction: undefined,
    netSalary: undefined,
    calcStatus: undefined,
    sealedBy: undefined,
    sealedTime: undefined,
    payDate: undefined,
    bankReceiptNo: undefined
  }
  proxy.resetForm('hrPayslipRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增工资条'
}

function handleUpdate(row) {
  reset()
  const payslipId = row.payslipId || ids.value[0]
  getHrPayslip(payslipId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改工资条'
  })
}

/** 查看 —— 使用独立查看弹窗 */
function handleView(row) {
  const payslipId = row.payslipId
  getHrPayslip(payslipId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['hrPayslipRef'].validate(valid => {
    if (valid) {
      if (form.value.payslipId != undefined) {
        updateHrPayslip(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrPayslip(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const payslipIds = row.payslipId || ids.value
  proxy.$modal.confirm('是否确认删除选中的工资条？').then(function() {
    return delHrPayslip(payslipIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/payslip/export', { ...queryParams.value }, `hrPayslip_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

/** ===== 人员/部门弹框选择 ===== */
const employeePickerRef = ref(null)
function openEmployeePicker() { employeePickerRef.value.open(form.value.employeeId); }
function onEmployeePickerConfirm(emp) {
  const f = form.value;
  f.employeeId = emp.employeeId;
  f.employeeName = emp.employeeName;
  // 档案信息自动带出：仅填充本页面表单中存在的字段
  const auto = { idCard: emp.idCard, gender: emp.gender, phone: emp.phone, email: emp.email, orgId: emp.orgId, orgName: emp.orgName, positionId: emp.positionId, positionName: emp.positionName };
  Object.keys(auto).forEach(k => { if (k in f && auto[k] !== null && auto[k] !== undefined) f[k] = auto[k]; });
}
function onEmployeeFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.employeeId) { form.value.employeeId = null; form.value.employeeName = ''; }
    else { openEmployeePicker(); }
    return;
  }
  openEmployeePicker();
}
const orgPickerRef = ref(null)
function openOrgPicker() { orgPickerRef.value.open(form.value.orgId); }
function onOrgPickerConfirm(dept) { form.value.orgId = dept.deptId; form.value.orgName = dept.deptName; }
function clearOrg() { form.value.orgId = null; form.value.orgName = ''; }
function onOrgFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.orgId) { clearOrg(); } else { openOrgPicker(); }
    return;
  }
  openOrgPicker();
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-payslip-page {
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
.hr-payslip-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-payslip-page .filter-card { padding:14px 20px 16px; }
.hr-payslip-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-payslip-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-payslip-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-payslip-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-payslip-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-payslip-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-payslip-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-payslip-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-payslip-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-payslip-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-payslip-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-payslip-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-payslip-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-payslip-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-payslip-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
/* el-input transparent inside .control */
.hr-payslip-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-payslip-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-payslip-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-payslip-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-payslip-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
/* el-select transparent inside .control */
.hr-payslip-page .field .control :deep(.el-select) { width:100%; }
.hr-payslip-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-payslip-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-payslip-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-payslip-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-payslip-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-payslip-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-payslip-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-payslip-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-payslip-page .btn-soft .el-icon { font-size:14px; }
.hr-payslip-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-payslip-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-payslip-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-payslip-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-payslip-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-payslip-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-payslip-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-payslip-page .table-wrap { overflow-x:auto; }
.hr-payslip-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-payslip-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-payslip-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-payslip-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-payslip-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-payslip-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-payslip-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-payslip-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-payslip-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-payslip-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-payslip-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-payslip-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-payslip-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-payslip-page .badge.amber .dot { background:var(--amber-500); }
.hr-payslip-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-payslip-page .badge.blue .dot { background:var(--blue-500); }
.hr-payslip-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-payslip-page .badge.green .dot { background:var(--green-500); }
.hr-payslip-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-payslip-page .badge.red .dot { background:var(--red-500); }
.hr-payslip-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-payslip-page .badge.violet .dot { background:var(--brand-500); }
.hr-payslip-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-payslip-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-payslip-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-payslip-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-payslip-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-payslip-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-payslip-page .pagination-container :deep(.el-pagination .btn-prev), .hr-payslip-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-payslip-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-payslip-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-payslip-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 详情弹窗（卡片式） ===== */
.hr-payslip-page .rd-page { max-height:60vh; overflow-y:auto; }
.hr-payslip-page .rd-card { border:1px solid #ebeef5; border-radius:8px; margin-bottom:16px; overflow:hidden; animation:rdFadeIn .4s ease-out forwards; }
.hr-payslip-page .rd-card-header { display:flex; align-items:center; justify-content:space-between; padding:12px 16px; background:#f5f7fa; cursor:pointer; user-select:none; }
.hr-payslip-page .rd-card-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:#303133; }
.hr-payslip-page .rd-card-icon { display:flex; align-items:center; justify-content:center; width:28px; height:28px; border-radius:6px; background:#fff; border:1px solid #e5e7eb; color:#409eff; }
.hr-payslip-page .rd-collapse-btn { border:none; background:transparent; cursor:pointer; padding:4px; display:flex; align-items:center; color:#909399; transition:transform .2s; }
.hr-payslip-page .rd-collapse-btn.is-collapsed { transform:rotate(-90deg); }
.hr-payslip-page .rd-card-body { padding:16px; }
.hr-payslip-page .rd-grid { display:grid; grid-template-columns:repeat(2, 1fr); gap:12px 24px; }
.hr-payslip-page .rd-item { display:flex; flex-direction:column; gap:4px; }
.hr-payslip-page .rd-item--full { grid-column:1 / -1; }
.hr-payslip-page .rd-label { font-size:12px; color:#909399; }
.hr-payslip-page .rd-value { font-size:14px; color:#303133; font-weight:500; }
.hr-payslip-page .rd-value--large { font-size:18px; font-weight:700; }
.hr-payslip-page .rd-value--muted { color:#9ca3af; font-style:italic; }
.hr-payslip-page .rd-amount { font-variant-numeric:tabular-nums; font-weight:700; color:#dc2626; }
.hr-payslip-page .rd-empty { display:flex; flex-direction:column; align-items:center; justify-content:center; padding:32px; color:#c0c4cc; }
.hr-payslip-page .rd-empty-icon { margin-bottom:8px; }
.hr-payslip-page .rd-empty-text { font-size:13px; margin:0; }
.hr-payslip-page .rd-detail-header { display:flex; align-items:center; gap:12px; }
.hr-payslip-page .rd-detail-header-icon { width:36px; height:36px; border-radius:8px; background:#ecf5ff; display:flex; align-items:center; justify-content:center; color:#409eff; }
.hr-payslip-page .rd-detail-header-main { flex:1; }
.hr-payslip-page .rd-detail-header-title { font-size:16px; font-weight:600; color:#303133; }
.hr-payslip-page .rd-detail-header-sub { display:flex; align-items:center; gap:12px; margin-top:4px; }
.hr-payslip-page .rd-detail-header-divider { width:1px; height:12px; background:#dcdfe6; }
.hr-payslip-page .rd-detail-header-no { font-size:13px; color:#909399; }
@keyframes rdFadeIn { from { opacity:0; transform:translateY(8px); } to { opacity:1; transform:translateY(0); } }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-payslip-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-payslip-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-payslip-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
