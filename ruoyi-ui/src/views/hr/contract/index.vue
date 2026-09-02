<template>
  <div class="app-container hr-contract-page">
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
          <label>合同编号</label>
          <div class="control">
            <el-input v-model="queryParams.contractNo" placeholder="请输入合同编号" clearable @keyup.enter="handleQuery">
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
          <label>合同类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.contractType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in hr_contract_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>开始日期</label>
          <div class="control">
            <el-date-picker v-model="startDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>结束日期</label>
          <div class="control">
            <el-date-picker v-model="endDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>签订日期</label>
          <div class="control">
            <el-date-picker v-model="signDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:contract:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:contract:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:contract:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:contract:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_contract_index"></right-toolbar></div>
      </div>
      <div class="expiry-alert" v-if="expiringList.length">
        <el-icon><BellFilled /></el-icon>
        <span>预警：检测到 <b>{{ expiringList.length }}</b> 份合同将在 3 天内到期：</span>
        <span class="expiry-chip" v-for="c in expiringList" :key="c.contractId">{{ c.employeeName }}（{{ c.endDate }} 到期{{ expiryLabel(c) }}）</span>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrContractList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" :width="colWidth('序号', 85)" align="center" />
          <el-table-column label="合同编号" prop="contractNo" key="contractNo" align="center" :width="colWidth('contractNo', 160)" resizable v-if="columns.contractNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.contractNo }}</span></template>
          </el-table-column>
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :width="colWidth('employeeName', 110)" resizable v-if="columns.employeeName.visible" />
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 110)" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo }}</span></template>
          </el-table-column>
          <el-table-column label="合同类型" prop="contractType" key="contractType" align="center" :width="colWidth('contractType', 120)" resizable v-if="columns.contractType.visible">
            <template #default="scope">
              <span class="badge" :class="contractTypeBadgeClass(scope.row.contractType)" v-if="scope.row.contractType !== undefined && scope.row.contractType !== null && scope.row.contractType !== ''">
                <span class="dot"></span>{{ contractTypeLabel(scope.row.contractType) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="开始日期" prop="startDate" key="startDate" align="center" :width="colWidth('startDate', 120)" resizable v-if="columns.startDate.visible" />
          <el-table-column label="结束日期" prop="endDate" key="endDate" align="center" :width="colWidth('endDate', 150)" resizable v-if="columns.endDate.visible">
<template #default="scope">
<span>{{ endDateDisplay(scope.row) }}</span>
<el-tag v-if="expiryType(scope.row) === 'soon'" type="danger" size="small" style="margin-left:4px">{{ expiryLabel(scope.row) }}</el-tag>
<el-tag v-else-if="expiryType(scope.row) === 'expired'" type="info" size="small" style="margin-left:4px">已过期</el-tag>
</template>
</el-table-column>
          <el-table-column label="签订日期" prop="signDate" key="signDate" align="center" :width="colWidth('signDate', 120)" resizable v-if="columns.signDate.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:contract:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:contract:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:contract:remove']">删除</el-button>
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
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrContractRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="合同编号" prop="contractNo">
                    <el-input v-model="form.contractNo" placeholder="自动生成" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="姓名" prop="employeeId">
                    <el-input :model-value="form.employeeName" readonly placeholder="请选择人员">
                      <template #append>
                        <el-button v-if="form.employeeId" icon="CircleClose" @click="onEmployeeFieldClick" />
                        <el-button v-else icon="Search" @click="onEmployeeFieldClick" />
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="工号" prop="employeeNo">
                    <el-input v-model="form.employeeNo" placeholder="选择人员后自动带出" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
<el-form-item label="合同类型" prop="contractType">
<el-select v-model="form.contractType" placeholder="请选择" clearable style="width: 100%" @change="onContractTypeChange"><el-option v-for="d in hr_contract_type" :key="d.value" :label="d.label" :value="d.value" /></el-select>
</el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>合同条款</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="开始日期" prop="startDate">
                    <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
<el-col :span="12">
<el-form-item label="结束日期" prop="endDate">
<el-date-picker v-if="form.contractType === '0'" v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
<el-input v-else :model-value="form.contractType === '1' ? '无固定期限，无结束日期' : '以任务完成为止'" readonly />
</el-form-item>
</el-col>
                <el-col :span="12">
                  <el-form-item label="签订日期" prop="signDate">
                    <el-date-picker v-model="form.signDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
<el-col :span="12" v-if="form.contractType !== '2'">
<el-form-item label="试用期月数" prop="probationMonths">
<el-input-number v-model="form.probationMonths" :precision="0" :min="0" :max="6" controls-position="right" style="width: 100%" />
</el-form-item>
</el-col>
<el-col :span="12" v-if="form.contractType !== '2'">
<el-form-item label="试用期工资" prop="probationSalary">
<el-input-number v-model="form.probationSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
</el-form-item>
</el-col>
                <el-col :span="12">
                  <el-form-item label="转正工资" prop="regularSalary">
                    <el-input-number v-model="form.regularSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="续签次数" prop="renewCount">
                    <el-input-number v-model="form.renewCount" :precision="0" :min="0" controls-position="right" style="width: 100%" />
                  </el-form-item>
                </el-col>
</el-row>
</div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="附件" prop="attachmentUrl">
                    <file-upload v-model="form.attachmentUrl" :limit="5" :file-type="['pdf', 'doc', 'docx', 'jpg', 'png']" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                    <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入备注" />
                  </el-form-item>
                </el-col>
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
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">劳动合同详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.contractNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">合同编号：{{ viewData.contractNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.vc0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ viewData.contractNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">合同类型</span><div class="rd-value"><dict-tag :options="hr_contract_type" :value="viewData.contractType" v-if="viewData.contractType !== undefined && viewData.contractType !== null && viewData.contractType !== ''" /><span v-else>-</span></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>合同期限</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.vc1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">开始日期</span><div class="rd-value">{{ viewData.startDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结束日期</span><div class="rd-value">{{ endDateDisplay(viewData) }}</div></div>
              <div class="rd-item"><span class="rd-label">签订日期</span><div class="rd-value">{{ viewData.signDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">试用期月数</span><div class="rd-value">{{ viewData.probationMonths != null ? viewData.probationMonths + ' 个月' : '-' }}</div></div>
<div class="rd-item"><span class="rd-label">续签次数</span><div class="rd-value">{{ viewData.renewCount != null ? viewData.renewCount + ' 次' : '-' }}</div></div>
</div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>薪资信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.vc2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">试用期工资</span><div class="rd-value">{{ viewData.probationSalary != null ? '￥' + viewData.probationSalary : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">转正工资</span><div class="rd-value">{{ viewData.regularSalary != null ? '￥' + viewData.regularSalary : '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.vc3">
            <div class="rd-grid">
<div class="rd-item rd-item--full">
<span class="rd-label">合同附件</span>
<div class="rd-value">
<div class="rd-file-links" v-if="viewData.attachmentUrl">
                    <div class="rd-file-item" v-for="(url, idx) in String(viewData.attachmentUrl).split(',')" :key="idx">
                      <div class="rd-file-link" @click="handleFilePreview(url)">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                        <span class="rd-file-name">{{ url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url }}</span>
                      </div>
                      <span class="rd-file-dl" @click="handleFileDownload(url)">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                        下载
                      </span>
                    </div>
                  </div>
                  <span v-else>-</span>
                </div>
              </div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <file-preview ref="filePreviewRef" />

    <!-- ===== 人员选择弹窗 ===== -->
    <employee-picker ref="employeePickerRef" title="选择人员" @confirm="onEmployeePickerConfirm" />
  </div>
</template>

<script setup name="HrContract">
import { listHrContract, getHrContract, addHrContract, updateHrContract, delHrContract } from '@/api/hr/hrContract'
import { useColumnResize } from '@/composables/useColumnResize'
import FilePreview from '@/components/FilePreview/index.vue'
import { downloadFile } from '@/utils/downloadFile'
import { BellFilled } from '@element-plus/icons-vue'
import EmployeePicker from '@/components/EmployeePicker'

const { proxy } = getCurrentInstance()
const { hr_contract_type } = proxy.useDict('hr_contract_type')

const hrContractList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const title = ref('')
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const showAdvanced = ref(false)
const startDateRange = ref([])
const endDateRange = ref([])
const signDateRange = ref([])
const filePreviewRef = ref(null)
const collapsedCards = reactive({ vc0: false, vc1: false, vc2: false, vc3: false, e0: false, e1: false, e2: false })
const employeePickerRef = ref(null)

/** 列显隐与列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_contract_index')

const defaultColumns = {
  contractNo: { label: '合同编号', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  employeeNo: { label: '工号', visible: true },
  contractType: { label: '合同类型', visible: true },
  startDate: { label: '开始日期', visible: true },
  endDate: { label: '结束日期', visible: true },
  signDate: { label: '签订日期', visible: true },
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_contract_index')
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

/** 按合同类型联动：无固定期限/任务型无结束日期，任务型不得约定试用期 */
function onContractTypeChange() {
const f = form.value
if (f.contractType !== '0') f.endDate = undefined
if (f.contractType === '2') { f.probationMonths = undefined; f.probationSalary = undefined }
proxy.$refs.hrContractRef && proxy.$refs.hrContractRef.clearValidate(['endDate', 'probationMonths', 'probationSalary'])
}

/** 详情中结束日期按合同类型展示（列表复用） */
function endDateDisplay(d) {
if (d.contractType === '1') return '无固定期限'
if (d.contractType === '2') return '以任务完成为止'
return d.endDate || '-'
}

/** 合同到期预警：提前 3 天标红，过期置灰 */
const DAY_MS = 24 * 3600 * 1000
function daysToEnd(row) {
if (!row.endDate) return null
const end = new Date(row.endDate + 'T00:00:00').getTime()
const today = new Date(); today.setHours(0, 0, 0, 0)
return Math.round((end - today.getTime()) / DAY_MS)
}
function expiryType(row) {
if (row.contractType === '1' || row.contractType === '2') return ''
const d = daysToEnd(row)
if (d === null) return ''
if (d < 0) return 'expired'
if (d <= 3) return 'soon'
return ''
}
function expiryLabel(row) {
const d = daysToEnd(row)
if (d === null) return ''
if (d < 0) return '已过期'
if (d === 0) return '今天到期'
return d + '天后到期'
}
const expiringList = computed(() => (hrContractList.value || []).filter(r => expiryType(r) === 'soon'))

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, contractNo: undefined, employeeName: undefined, employeeNo: undefined, contractType: undefined },
rules: {
employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }],
startDate: [{ required: true, message: '开始日期不能为空', trigger: 'blur' }],
endDate: [{ validator: (rule, value, cb) => { if (form.value.contractType === '0' && !value) return cb(new Error('固定期限合同结束日期不能为空')); cb() }, trigger: 'change' }]
}
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.contractNo) count++
  if (queryParams.value.employeeName) count++
  if (queryParams.value.employeeNo) count++
  if (queryParams.value.contractType) count++
  if (startDateRange.value && startDateRange.value.length === 2) count++
  if (endDateRange.value && endDateRange.value.length === 2) count++
  if (signDateRange.value && signDateRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  listHrContract(queryParams.value).then(response => {
    hrContractList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, startDateRange.value, 'StartDate')
  proxy.addDateRange(queryParams.value, endDateRange.value, 'EndDate')
  proxy.addDateRange(queryParams.value, signDateRange.value, 'SignDate')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.contractNo = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  queryParams.value.contractType = undefined
  startDateRange.value = []
  endDateRange.value = []
  signDateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function handleFilePreview(url) {
  const name = url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url
  proxy.$refs.filePreviewRef.open(url, name)
}

function handleFileDownload(url) {
  downloadFile(url)
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.contractId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    contractNo: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    contractType: undefined,
    startDate: undefined,
    endDate: undefined,
    signDate: undefined,
    probationMonths: undefined,
    probationSalary: undefined,
    regularSalary: undefined,
renewCount: undefined,
attachmentUrl: undefined,
remark: undefined
}
  proxy.resetForm('hrContractRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增劳动合同'
}

function handleUpdate(row) {
  reset()
  const contractId = row.contractId || ids.value[0]
  getHrContract(contractId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改劳动合同'
  })
}

function handleView(row) {
  const contractId = row.contractId
  getHrContract(contractId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['hrContractRef'].validate(valid => {
    if (valid) {
      if (form.value.contractId != undefined) {
        updateHrContract(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrContract(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const contractIds = row.contractId || ids.value
  proxy.$modal.confirm('是否确认删除选中的劳动合同？').then(function() {
    return delHrContract(contractIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/contract/export', { ...queryParams.value }, `hrContract_${new Date().getTime()}.xlsx`)
}

/** 合同类型文字映射 */
function contractTypeLabel(v) {
  if (v === undefined || v === null || v === '') return '-'
  const d = (hr_contract_type.value || []).find(i => i.value == v)
  return d ? d.label : String(v)
}
function contractTypeBadgeClass(v) {
  const map = { '0': 'blue', '1': 'green', '2': 'violet' }
  return map[v] || 'gray'
}

/** ===== 人员弹框选择 ===== */
function openEmployeePicker() { employeePickerRef.value.open(form.value.employeeId); }
function onEmployeePickerConfirm(emp) {
  const f = form.value;
  f.employeeId = emp.employeeId;
  f.employeeName = emp.employeeName;
  // 档案信息自动带出：仅填充本页面表单中存在的字段
  const auto = { idCard: emp.idCard, gender: emp.gender, phone: emp.phone, email: emp.email, orgId: emp.orgId, orgName: emp.orgName, positionId: emp.positionId, positionName: emp.positionName, employeeNo: emp.employeeNo };
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
.hr-contract-page {
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
.hr-contract-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-contract-page .filter-card { padding:14px 20px 16px; }
.hr-contract-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-contract-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-contract-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-contract-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-contract-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-contract-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-contract-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-contract-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-contract-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-contract-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-contract-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-contract-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-contract-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-contract-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-contract-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-contract-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-contract-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-contract-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-contract-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-contract-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-contract-page .field .control :deep(.el-select) { width:100%; }
.hr-contract-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-contract-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-contract-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-contract-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-contract-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-contract-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-contract-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-contract-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-contract-page .btn-soft .el-icon { font-size:14px; }
.hr-contract-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-contract-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-contract-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-contract-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-contract-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-contract-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-contract-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-contract-page .table-wrap { overflow-x:auto; }
.hr-contract-page .expiry-alert { display:flex; align-items:center; flex-wrap:wrap; gap:8px; margin:0 0 12px; padding:10px 14px; border-radius:10px; background:#fef2f2; border:1px solid #fecaca; color:#b91c1c; font-size:13px; }
.hr-contract-page .expiry-alert .el-icon { font-size:16px; }
.hr-contract-page .expiry-alert b { color:#dc2626; }
.hr-contract-page .expiry-chip { padding:2px 10px; border-radius:999px; background:#fff; border:1px solid #fca5a5; color:#b91c1c; font-size:12px; }
.hr-contract-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-contract-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-contract-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-contract-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-contract-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-contract-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-contract-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-contract-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-contract-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-contract-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-contract-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-contract-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-contract-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-contract-page .badge.amber .dot { background:var(--amber-500); }
.hr-contract-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-contract-page .badge.blue .dot { background:var(--blue-500); }
.hr-contract-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-contract-page .badge.green .dot { background:var(--green-500); }
.hr-contract-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-contract-page .badge.red .dot { background:var(--red-500); }
.hr-contract-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-contract-page .badge.violet .dot { background:var(--brand-500); }
.hr-contract-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-contract-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-contract-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-contract-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-contract-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-contract-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-contract-page .pagination-container :deep(.el-pagination .btn-prev), .hr-contract-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-contract-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-contract-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-contract-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 查看详情弹窗（rd-*) ===== */
.hr-contract-page .rd-page {
  max-height: 60vh;
  overflow-y: auto;
}
.hr-contract-page .rd-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
  animation: rdFadeIn 0.4s ease-out forwards;
}
.hr-contract-page .rd-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f5f7fa;
  cursor: pointer;
  user-select: none;
}
.hr-contract-page .rd-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.hr-contract-page .rd-card-icon {
  display: flex;
  align-items: center;
  color: #409eff;
}
.hr-contract-page .rd-collapse-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  color: #909399;
  transition: transform 0.2s;
}
.hr-contract-page .rd-collapse-btn.is-collapsed {
  transform: rotate(-90deg);
}
.hr-contract-page .rd-card-body {
  padding: 16px;
}
.hr-contract-page .rd-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 24px;
}
.hr-contract-page .rd-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.hr-contract-page .rd-item--full {
  grid-column: 1 / -1;
}
.hr-contract-page .rd-label {
  font-size: 12px;
  color: #909399;
}
.hr-contract-page .rd-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.hr-contract-page .rd-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  color: #c0c4cc;
}
.hr-contract-page .rd-empty-icon {
  margin-bottom: 8px;
}
.hr-contract-page .rd-empty-text {
  font-size: 13px;
  margin: 0;
}
.hr-contract-page .rd-detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.hr-contract-page .rd-detail-header-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
}
.hr-contract-page .rd-detail-header-main {
  flex: 1;
}
.hr-contract-page .rd-detail-header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.hr-contract-page .rd-detail-header-sub {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}
.hr-contract-page .rd-detail-header-divider {
  width: 1px;
  height: 12px;
  background: #dcdfe6;
}
.hr-contract-page .rd-detail-header-no {
  font-size: 13px;
  color: #909399;
}

/* ===== rd-dialog header 覆盖（全局 detail-page.scss 兜底） ===== */
:deep(.rd-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top: 16px; right: 16px; z-index: 10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }

@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-contract-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-contract-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-contract-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
