<template>
  <div class="app-container hr-salaryPlan-page">
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
          <label>定薪单号</label>
          <div class="control">
            <el-input v-model="queryParams.planNo" placeholder="请输入定薪单号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
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
          <label>类型</label>
          <div class="control">
            <el-input v-model="queryParams.planType" placeholder="请输入类型" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>审批状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.auditStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in hr_audit_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>审批人</label>
          <div class="control">
            <el-input v-model="queryParams.auditBy" placeholder="请输入审批人" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>生效日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:salaryPlan:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:salaryPlan:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:salaryPlan:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:salaryPlan:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_salaryPlan_index"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrSalaryPlanList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" :width="colWidth('序号', 85)" align="center" />
          <el-table-column label="定薪单号" prop="planNo" key="planNo" align="center" :width="colWidth('planNo', 180)" resizable v-if="columns.planNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.planNo }}</span></template>
          </el-table-column>
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 120)" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo }}</span></template>
          </el-table-column>
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :width="colWidth('employeeName', 100)" resizable v-if="columns.employeeName.visible" />
          <el-table-column label="类型" prop="planType" key="planType" align="center" :width="colWidth('planType', 110)" resizable v-if="columns.planType.visible">
            <template #default="scope">
              <span class="badge violet" v-if="scope.row.planType">{{ scope.row.planType }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="生效日期" prop="effectiveDate" key="effectiveDate" align="center" :width="colWidth('effectiveDate', 120)" resizable v-if="columns.effectiveDate.visible" />
          <el-table-column label="基本工资" prop="basicSalary" key="basicSalary" align="center" :width="colWidth('basicSalary', 130)" resizable v-if="columns.basicSalary.visible" />
          <el-table-column label="岗位工资" prop="positionSalary" key="positionSalary" align="center" :width="colWidth('positionSalary', 130)" resizable v-if="columns.positionSalary.visible" />
          <el-table-column label="薪资合计" prop="totalSalary" key="totalSalary" align="center" :width="colWidth('totalSalary', 140)" resizable v-if="columns.totalSalary.visible" />
          <el-table-column label="审批状态" prop="auditStatus" key="auditStatus" align="center" :width="colWidth('auditStatus', 110)" resizable v-if="columns.auditStatus.visible">
            <template #default="scope">
              <span class="badge" :class="auditBadgeClass(scope.row.auditStatus)" v-if="scope.row.auditStatus !== undefined && scope.row.auditStatus !== null && scope.row.auditStatus !== ''">
                <span class="dot"></span>{{ auditLabel(scope.row.auditStatus) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:salaryPlan:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:salaryPlan:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:salaryPlan:remove']">删除</el-button>
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
    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1v22"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrSalaryPlanRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 卡片：基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="定薪单号" prop="planNo"><el-input v-model="form.planNo" placeholder="自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="姓名" prop="employeeId">
                  <el-input :model-value="form.employeeName" readonly placeholder="请选择人员">
                    <template #append>
                      <el-button v-if="form.employeeId" icon="CircleClose" @click="onEmployeeFieldClick" />
                      <el-button v-else icon="Search" @click="onEmployeeFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工号" prop="employeeNo"><el-input v-model="form.employeeNo" placeholder="请输入工号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="类型" prop="planType"><el-input v-model="form.planType" placeholder="请输入类型" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="生效日期" prop="effectiveDate"><el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 卡片：薪资标准 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>薪资标准</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="基本工资" prop="basicSalary"><el-input-number v-model="form.basicSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="岗位工资" prop="positionSalary"><el-input-number v-model="form.positionSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="技能津贴" prop="skillAllowance"><el-input-number v-model="form.skillAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工龄工资" prop="seniorityPay"><el-input-number v-model="form.seniorityPay" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="夜班津贴标准" prop="nightAllowance"><el-input-number v-model="form.nightAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="高温津贴标准" prop="highTempAllowance"><el-input-number v-model="form.highTempAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="有毒有害津贴" prop="hazardAllowance"><el-input-number v-model="form.hazardAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="餐补标准" prop="mealAllowance"><el-input-number v-model="form.mealAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="交通补贴" prop="transportAllowance"><el-input-number v-model="form.transportAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="全勤奖" prop="fullAttendanceBonus"><el-input-number v-model="form.fullAttendanceBonus" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="绩效奖金" prop="performanceBonus"><el-input-number v-model="form.performanceBonus" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="其他补贴" prop="otherAllowance"><el-input-number v-model="form.otherAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="薪资合计" prop="totalSalary"><el-input-number v-model="form.totalSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 卡片：审批信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审批信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="审批状态" prop="auditStatus"><el-select v-model="form.auditStatus" placeholder="请选择" style="width: 100%"><el-option v-for="d in hr_audit_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="审批人" prop="auditBy"><el-input v-model="form.auditBy" placeholder="请输入审批人" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="审批时间" prop="auditTime"><el-date-picker v-model="form.auditTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="审批意见" prop="auditRemark"><el-input v-model="form.auditRemark" placeholder="请输入审批意见" /></el-form-item></el-col>
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
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1v22"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">定薪调薪详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.planNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">定薪单号：{{ viewData.planNo }}</span>
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
              <div class="rd-item"><span class="rd-label">定薪单号</span><div class="rd-value">{{ viewData.planNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">类型</span><div class="rd-value">{{ planTypeLabel(viewData.planType) }}</div></div>
              <div class="rd-item"><span class="rd-label">生效日期</span><div class="rd-value">{{ viewData.effectiveDate || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1v22"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>薪资标准</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">基本工资</span><div class="rd-value rd-amount">{{ formatAmount(viewData.basicSalary) }}</div></div>
              <div class="rd-item"><span class="rd-label">岗位工资</span><div class="rd-value rd-amount">{{ formatAmount(viewData.positionSalary) }}</div></div>
              <div class="rd-item"><span class="rd-label">技能津贴</span><div class="rd-value rd-amount">{{ formatAmount(viewData.skillAllowance) }}</div></div>
              <div class="rd-item"><span class="rd-label">工龄工资</span><div class="rd-value rd-amount">{{ formatAmount(viewData.seniorityPay) }}</div></div>
              <div class="rd-item"><span class="rd-label">夜班津贴标准</span><div class="rd-value rd-amount">{{ formatAmount(viewData.nightAllowance) }}</div></div>
              <div class="rd-item"><span class="rd-label">高温津贴标准</span><div class="rd-value rd-amount">{{ formatAmount(viewData.highTempAllowance) }}</div></div>
              <div class="rd-item"><span class="rd-label">有毒有害津贴</span><div class="rd-value rd-amount">{{ formatAmount(viewData.hazardAllowance) }}</div></div>
              <div class="rd-item"><span class="rd-label">餐补标准</span><div class="rd-value rd-amount">{{ formatAmount(viewData.mealAllowance) }}</div></div>
              <div class="rd-item"><span class="rd-label">交通补贴</span><div class="rd-value rd-amount">{{ formatAmount(viewData.transportAllowance) }}</div></div>
              <div class="rd-item"><span class="rd-label">全勤奖</span><div class="rd-value rd-amount">{{ formatAmount(viewData.fullAttendanceBonus) }}</div></div>
              <div class="rd-item"><span class="rd-label">绩效奖金</span><div class="rd-value rd-amount">{{ formatAmount(viewData.performanceBonus) }}</div></div>
              <div class="rd-item"><span class="rd-label">其他补贴</span><div class="rd-value rd-amount">{{ formatAmount(viewData.otherAllowance) }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">薪资合计</span><div class="rd-value rd-value--large rd-amount">{{ formatAmount(viewData.totalSalary) }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.auditStatus || viewData.auditBy || viewData.auditTime || viewData.auditRemark">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审批信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': viewData.auditStatus === '1', 'rd-timeline-dot--error': viewData.auditStatus === '2' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title"><dict-tag :options="hr_audit_status" :value="viewData.auditStatus" /></span>
                    <span class="rd-timeline-time">{{ viewData.auditTime || '-' }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value">{{ viewData.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="viewData.auditRemark"><strong>审批意见：</strong>{{ viewData.auditRemark }}</div>
                </div>
              </div>
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
  </div>
</template>

<script setup name="HrSalaryPlan">
import { listHrSalaryPlan, getHrSalaryPlan, addHrSalaryPlan, updateHrSalaryPlan, delHrSalaryPlan } from '@/api/hr/hrSalaryPlan'
import { Filter, ArrowDown, Search, Edit, Delete, Download } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'
import EmployeePicker from '@/components/EmployeePicker'

const { proxy } = getCurrentInstance()

const { hr_audit_status } = proxy.useDict('hr_audit_status')

const hrSalaryPlanList = ref([])
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
const dateRange = ref([])
const collapsedCards = reactive({ c0: false, c1: false, c2: false })

/** 列显隐与列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_salaryPlan_index')

const defaultColumns = {
  planNo: { label: '定薪单号', visible: true },
  employeeNo: { label: '工号', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  planType: { label: '类型', visible: true },
  effectiveDate: { label: '生效日期', visible: true },
  basicSalary: { label: '基本工资', visible: true },
  positionSalary: { label: '岗位工资', visible: true },
  totalSalary: { label: '薪资合计', visible: true },
  auditStatus: { label: '审批状态', visible: true },
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_salaryPlan_index')
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
  queryParams: { pageNum: 1, pageSize: 10, planNo: undefined, employeeName: undefined, employeeNo: undefined, planType: undefined, auditStatus: undefined, auditBy: undefined },
  rules: {
    employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }],
    effectiveDate: [{ required: true, message: '生效日期不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.planNo) count++
  if (queryParams.value.employeeName) count++
  if (queryParams.value.employeeNo) count++
  if (queryParams.value.planType) count++
  if (queryParams.value.auditStatus) count++
  if (queryParams.value.auditBy) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

const viewData = ref({})

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

/** 类型字面映射（库内存编码，无字典）：0入职定薪 1转正调薪 2岗变调薪 3年度普调 4个案调薪 */
function planTypeLabel(v) {
  if (v === undefined || v === null || v === '') return '-'
  const map = { '0': '入职定薪', '1': '转正调薪', '2': '岗变调薪', '3': '年度普调', '4': '个案调薪' }
  return map[String(v)] || String(v)
}

function auditLabel(v) {
  const d = (hr_audit_status.value || []).find(i => i.value == v)
  return d ? d.label : String(v)
}
function auditBadgeClass(v) {
  const map = { '0': 'amber', '1': 'green', '2': 'red' }
  return map[v] || 'gray'
}

function formatAmount(val) {
  if (val === null || val === undefined || val === '') return '-'
  return '￥' + Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getList() {
  loading.value = true
  listHrSalaryPlan(queryParams.value).then(response => {
    hrSalaryPlanList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, dateRange.value, 'EffectiveDate')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.planNo = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  queryParams.value.planType = undefined
  queryParams.value.auditStatus = undefined
  queryParams.value.auditBy = undefined
  dateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.planId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    planNo: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    planType: undefined,
    effectiveDate: undefined,
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
    performanceBonus: undefined,
    otherAllowance: undefined,
    totalSalary: undefined,
    auditStatus: undefined,
    auditBy: undefined,
    auditTime: undefined,
    auditRemark: undefined
  }
  proxy.resetForm('hrSalaryPlanRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增定薪调薪'
}

function handleUpdate(row) {
  reset()
  const planId = row.planId || ids.value[0]
  getHrSalaryPlan(planId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改定薪调薪'
  })
}

/** 查看 —— 使用独立查看弹窗 */
function handleView(row) {
  const planId = row.planId
  getHrSalaryPlan(planId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['hrSalaryPlanRef'].validate(valid => {
    if (valid) {
      if (form.value.planId != undefined) {
        updateHrSalaryPlan(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrSalaryPlan(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const planIds = row.planId || ids.value
  proxy.$modal.confirm('是否确认删除选中的定薪调薪？').then(function() {
    return delHrSalaryPlan(planIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/salaryPlan/export', { ...queryParams.value }, `hrSalaryPlan_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

/** ===== 人员弹框选择 ===== */
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

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-salaryPlan-page {
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
.hr-salaryPlan-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-salaryPlan-page .filter-card { padding:14px 20px 16px; }
.hr-salaryPlan-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-salaryPlan-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-salaryPlan-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-salaryPlan-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-salaryPlan-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-salaryPlan-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-salaryPlan-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-salaryPlan-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-salaryPlan-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-salaryPlan-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-salaryPlan-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-salaryPlan-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-salaryPlan-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-salaryPlan-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-salaryPlan-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
/* el-input transparent inside .control */
.hr-salaryPlan-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-salaryPlan-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-salaryPlan-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-salaryPlan-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-salaryPlan-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
/* el-select transparent inside .control */
.hr-salaryPlan-page .field .control :deep(.el-select) { width:100%; }
.hr-salaryPlan-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-salaryPlan-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-salaryPlan-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-salaryPlan-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-salaryPlan-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-salaryPlan-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-salaryPlan-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-salaryPlan-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-salaryPlan-page .btn-soft .el-icon { font-size:14px; }
.hr-salaryPlan-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-salaryPlan-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-salaryPlan-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-salaryPlan-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-salaryPlan-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-salaryPlan-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-salaryPlan-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-salaryPlan-page .table-wrap { overflow-x:auto; }
.hr-salaryPlan-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-salaryPlan-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-salaryPlan-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-salaryPlan-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-salaryPlan-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-salaryPlan-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-salaryPlan-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-salaryPlan-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-salaryPlan-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-salaryPlan-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-salaryPlan-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-salaryPlan-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-salaryPlan-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-salaryPlan-page .badge.amber .dot { background:var(--amber-500); }
.hr-salaryPlan-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-salaryPlan-page .badge.blue .dot { background:var(--blue-500); }
.hr-salaryPlan-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-salaryPlan-page .badge.green .dot { background:var(--green-500); }
.hr-salaryPlan-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-salaryPlan-page .badge.red .dot { background:var(--red-500); }
.hr-salaryPlan-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-salaryPlan-page .badge.violet .dot { background:var(--brand-500); }
.hr-salaryPlan-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-salaryPlan-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-salaryPlan-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-salaryPlan-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-salaryPlan-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-salaryPlan-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-salaryPlan-page .pagination-container :deep(.el-pagination .btn-prev), .hr-salaryPlan-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-salaryPlan-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-salaryPlan-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-salaryPlan-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 详情弹窗（卡片式） ===== */
.hr-salaryPlan-page .rd-page { max-height:60vh; overflow-y:auto; }
.hr-salaryPlan-page .rd-card { border:1px solid #ebeef5; border-radius:8px; margin-bottom:16px; overflow:hidden; animation:rdFadeIn .4s ease-out forwards; }
.hr-salaryPlan-page .rd-card-header { display:flex; align-items:center; justify-content:space-between; padding:12px 16px; background:#f5f7fa; cursor:pointer; user-select:none; }
.hr-salaryPlan-page .rd-card-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:#303133; }
.hr-salaryPlan-page .rd-card-icon { display:flex; align-items:center; justify-content:center; width:28px; height:28px; border-radius:6px; background:#fff; border:1px solid #e5e7eb; color:#409eff; }
.hr-salaryPlan-page .rd-collapse-btn { border:none; background:transparent; cursor:pointer; padding:4px; display:flex; align-items:center; color:#909399; transition:transform .2s; }
.hr-salaryPlan-page .rd-collapse-btn.is-collapsed { transform:rotate(-90deg); }
.hr-salaryPlan-page .rd-card-body { padding:16px; }
.hr-salaryPlan-page .rd-grid { display:grid; grid-template-columns:repeat(2, 1fr); gap:12px 24px; }
.hr-salaryPlan-page .rd-item { display:flex; flex-direction:column; gap:4px; }
.hr-salaryPlan-page .rd-item--full { grid-column:1 / -1; }
.hr-salaryPlan-page .rd-label { font-size:12px; color:#909399; }
.hr-salaryPlan-page .rd-value { font-size:14px; color:#303133; font-weight:500; }
.hr-salaryPlan-page .rd-value--large { font-size:18px; font-weight:700; }
.hr-salaryPlan-page .rd-value--muted { color:#9ca3af; font-style:italic; }
.hr-salaryPlan-page .rd-amount { font-variant-numeric:tabular-nums; font-weight:700; color:#dc2626; }
.hr-salaryPlan-page .rd-empty { display:flex; flex-direction:column; align-items:center; justify-content:center; padding:32px; color:#c0c4cc; }
.hr-salaryPlan-page .rd-empty-icon { margin-bottom:8px; }
.hr-salaryPlan-page .rd-empty-text { font-size:13px; margin:0; }
.hr-salaryPlan-page .rd-detail-header { display:flex; align-items:center; gap:12px; }
.hr-salaryPlan-page .rd-detail-header-icon { width:36px; height:36px; border-radius:8px; background:#ecf5ff; display:flex; align-items:center; justify-content:center; color:#409eff; }
.hr-salaryPlan-page .rd-detail-header-main { flex:1; }
.hr-salaryPlan-page .rd-detail-header-title { font-size:16px; font-weight:600; color:#303133; }
.hr-salaryPlan-page .rd-detail-header-sub { display:flex; align-items:center; gap:12px; margin-top:4px; }
.hr-salaryPlan-page .rd-detail-header-divider { width:1px; height:12px; background:#dcdfe6; }
.hr-salaryPlan-page .rd-detail-header-no { font-size:13px; color:#909399; }
.hr-salaryPlan-page .rd-timeline { position:relative; padding-left:24px; }
.hr-salaryPlan-page .rd-timeline::before { content:''; position:absolute; left:10px; top:8px; bottom:8px; width:2px; background:linear-gradient(to bottom, #d1d5db, #e5e7eb); border-radius:1px; }
.hr-salaryPlan-page .rd-timeline-item { position:relative; padding-bottom:10px; }
.hr-salaryPlan-page .rd-timeline-item:last-child { padding-bottom:0; }
.hr-salaryPlan-page .rd-timeline-dot { position:absolute; left:-24px; top:4px; width:20px; height:20px; border-radius:50%; background:#fff; border:3px solid #9ca3af; display:flex; align-items:center; justify-content:center; z-index:1; }
.hr-salaryPlan-page .rd-timeline-dot--success { border-color:#10b981; box-shadow:0 0 0 4px #ecfdf5; }
.hr-salaryPlan-page .rd-timeline-dot--error { border-color:#ef4444; box-shadow:0 0 0 4px #fef2f2; }
.hr-salaryPlan-page .rd-timeline-content { background:#f9fafb; border-radius:10px; padding:10px; border:1px solid #f3f4f6; }
.hr-salaryPlan-page .rd-timeline-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:6px; }
.hr-salaryPlan-page .rd-timeline-title { font-weight:600; color:#111827; font-size:14px; }
.hr-salaryPlan-page .rd-timeline-time { font-size:12px; color:#6b7280; }
.hr-salaryPlan-page .rd-timeline-body { display:grid; grid-template-columns:repeat(2, 1fr); gap:10px 20px; }
.hr-salaryPlan-page .rd-timeline-comment { margin-top:6px; padding:10px; background:#fff; border-radius:8px; border-left:3px solid #f59e0b; font-size:14px; color:#374151; line-height:1.6; }
@keyframes rdFadeIn { from { opacity:0; transform:translateY(8px); } to { opacity:1; transform:translateY(0); } }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-salaryPlan-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-salaryPlan-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-salaryPlan-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
