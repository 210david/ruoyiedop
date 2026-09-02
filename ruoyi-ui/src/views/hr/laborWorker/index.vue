<template>
  <div class="app-container hr-laborworker-page">
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
          <label>姓名</label>
          <div class="control">
            <el-input v-model="queryParams.workerName" placeholder="请输入姓名" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>身份证号</label>
          <div class="control">
            <el-input v-model="queryParams.idCard" placeholder="请输入身份证号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>联系电话</label>
          <div class="control">
            <el-input v-model="queryParams.phone" placeholder="请输入联系电话" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>用工形态</label>
          <div class="control">
            <el-select v-model="queryParams.workerType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in laborTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>派遣公司</label>
          <div class="control">
            <el-input v-model="queryParams.dispatchCompany" placeholder="请输入派遣公司" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>开户银行</label>
          <div class="control">
            <el-input v-model="queryParams.bankName" placeholder="请输入开户银行" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>银行卡号</label>
          <div class="control">
            <el-input v-model="queryParams.bankAccount" placeholder="请输入银行卡号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:laborWorker:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:laborWorker:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:laborWorker:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:laborWorker:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_laborWorker_index"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrLaborWorkerList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" :width="colWidth('序号', 85)" align="center" />
          <el-table-column label="姓名" prop="workerName" key="workerName" align="center" :width="colWidth('workerName', 110)" resizable v-if="columns.workerName.visible" />
          <el-table-column label="身份证号" prop="idCard" key="idCard" align="center" :width="colWidth('idCard', 180)" resizable show-overflow-tooltip v-if="columns.idCard.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.idCard }}</span></template>
          </el-table-column>
          <el-table-column label="联系电话" prop="phone" key="phone" align="center" :width="colWidth('phone', 120)" resizable v-if="columns.phone.visible" />
          <el-table-column label="用工形态" prop="workerType" key="workerType" align="center" :width="colWidth('workerType', 110)" resizable v-if="columns.workerType.visible">
            <template #default="scope">
              <span v-if="scope.row.workerType" class="badge" :class="dictBadgeClass(laborTypeOptions, scope.row.workerType, 'blue')"><span class="dot"></span>{{ dictLabel(laborTypeOptions, scope.row.workerType) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="派遣公司" prop="dispatchCompany" key="dispatchCompany" align="center" :width="colWidth('dispatchCompany', 180)" resizable show-overflow-tooltip v-if="columns.dispatchCompany.visible" />
          <el-table-column label="开户银行" prop="bankName" key="bankName" align="center" :width="colWidth('bankName', 150)" resizable v-if="columns.bankName.visible" />
          <el-table-column label="银行卡号" prop="bankAccount" key="bankAccount" align="center" :width="colWidth('bankAccount', 160)" resizable v-if="columns.bankAccount.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.bankAccount }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:laborWorker:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:laborWorker:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:laborWorker:remove']">删除</el-button>
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
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrLaborWorkerRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="姓名" prop="workerName">
                    <el-input v-model="form.workerName" placeholder="请输入姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="身份证号" prop="idCard">
                    <el-input v-model="form.idCard" placeholder="请输入身份证号" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系电话" prop="phone">
                    <el-input v-model="form.phone" placeholder="请输入联系电话" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="用工形态" prop="workerType">
                    <el-select v-model="form.workerType" placeholder="请选择" clearable style="width: 100%">
                      <el-option v-for="d in laborTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="派遣公司" prop="dispatchCompany">
                    <el-input v-model="form.dispatchCompany" placeholder="请输入派遣公司" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>劳务信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="部门" prop="orgId">
                  <el-input :model-value="form.orgName" readonly placeholder="请选择部门">
                    <template #append>
                      <el-button v-if="form.orgId" icon="CircleClose" @click="onOrgFieldClick" />
                      <el-button v-else icon="Search" @click="onOrgFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="岗位" prop="positionId">
                  <el-input :model-value="form.positionName" readonly placeholder="请选择岗位">
                    <template #append>
                      <el-button v-if="form.positionId" icon="CircleClose" @click="onPositionFieldClick" />
                      <el-button v-else icon="Search" @click="onPositionFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12">
                  <el-form-item label="用工开始日期" prop="startDate">
                    <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="用工结束日期" prop="endDate">
                    <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="协议编号" prop="agreementNo">
                    <el-input v-model="form.agreementNo" placeholder="自动生成" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>结算信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="结算方式" prop="payType">
                    <el-input v-model="form.payType" placeholder="请输入结算方式" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="日薪标准" prop="dailyWage">
                    <el-input-number v-model="form.dailyWage" :precision="2" :min="0" controls-position="right" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="开户银行" prop="bankName">
                    <el-input v-model="form.bankName" placeholder="请输入开户银行" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="银行卡号" prop="bankAccount">
                    <el-input v-model="form.bankAccount" placeholder="请输入银行卡号" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e3">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="是否转正" prop="toRegular">
                    <el-input v-model="form.toRegular" placeholder="请输入是否转正" />
                  </el-form-item>
                </el-col>
                <el-col :span="12"><el-form-item label="转正后员工" prop="employeeId">
                  <el-input :model-value="form.employeeName" readonly placeholder="请选择人员">
                    <template #append>
                      <el-button v-if="form.employeeId" icon="CircleClose" @click="onEmployeeFieldClick" />
                      <el-button v-else icon="Search" @click="onEmployeeFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
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
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">劳务用工详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.workerName">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">姓名：{{ viewData.workerName }}</span>
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
              <div class="rd-item"><span class="rd-label">姓名</span><div class="rd-value">{{ viewData.workerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">身份证号</span><div class="rd-value">{{ viewData.idCard || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">联系电话</span><div class="rd-value">{{ viewData.phone || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">用工形态</span><div class="rd-value"><dict-tag :options="laborTypeOptions" :value="viewData.workerType" /></div></div>
              <div class="rd-item"><span class="rd-label">派遣公司</span><div class="rd-value">{{ viewData.dispatchCompany || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>用工信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.vc1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">岗位</span><div class="rd-value">{{ viewData.positionName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">用工开始日期</span><div class="rd-value">{{ viewData.startDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">用工结束日期</span><div class="rd-value">{{ viewData.endDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">协议编号</span><div class="rd-value">{{ viewData.agreementNo || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>结算信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.vc2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">结算方式</span><div class="rd-value">{{ viewData.payType || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">日薪标准</span><div class="rd-value">{{ viewData.dailyWage != null ? '￥' + viewData.dailyWage : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">开户银行</span><div class="rd-value">{{ viewData.bankName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">银行卡号</span><div class="rd-value">{{ viewData.bankAccount || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>转正信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.vc3">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">是否转正</span><div class="rd-value">{{ viewData.toRegular || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
    <!-- ===== 人员/部门/岗位选择弹窗 ===== -->
    <employee-picker ref="employeePickerRef" title="选择人员" @confirm="onEmployeePickerConfirm" />
    <dept-picker ref="orgPickerRef" title="选择部门" @confirm="onOrgPickerConfirm" />
    <position-picker ref="positionPickerRef" title="选择岗位" @confirm="onPositionPickerConfirm" />
  </div>
</template>

<script setup name="HrLaborWorker">
import { listHrLaborWorker, getHrLaborWorker, addHrLaborWorker, updateHrLaborWorker, delHrLaborWorker } from '@/api/hr/hrLaborWorker'
import { useColumnResize } from '@/composables/useColumnResize'
import EmployeePicker from '@/components/EmployeePicker';
import DeptPicker from '@/components/DeptPicker';
import PositionPicker from '@/components/PositionPicker';

const { proxy } = getCurrentInstance()

const { hr_labor_type: laborTypeOptions } = proxy.useDict('hr_labor_type')

const employeePickerRef = ref(null)
const orgPickerRef = ref(null)
const positionPickerRef = ref(null)

const hrLaborWorkerList = ref([])
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
const collapsedCards = reactive({ vc0: false, vc1: false, vc2: false, vc3: false, e0: false, e1: false, e2: false, e3: false })

/** 列显隐与列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_laborWorker_index')

const defaultColumns = {
  workerName: { label: '姓名', visible: true },
  idCard: { label: '身份证号', visible: true },
  phone: { label: '联系电话', visible: true },
  workerType: { label: '用工形态', visible: true },
  dispatchCompany: { label: '派遣公司', visible: true },
  bankName: { label: '开户银行', visible: true },
  bankAccount: { label: '银行卡号', visible: true },
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_laborWorker_index')
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
  queryParams: { pageNum: 1, pageSize: 10, workerName: undefined, idCard: undefined, phone: undefined, workerType: undefined, dispatchCompany: undefined, bankName: undefined, bankAccount: undefined },
  rules: {
    workerName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
    startDate: [{ required: true, message: '用工开始日期不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.workerName) count++
  if (queryParams.value.idCard) count++
  if (queryParams.value.phone) count++
  if (queryParams.value.workerType) count++
  if (queryParams.value.dispatchCompany) count++
  if (queryParams.value.bankName) count++
  if (queryParams.value.bankAccount) count++
  return count
})

function getList() {
  loading.value = true
  listHrLaborWorker(queryParams.value).then(response => {
    hrLaborWorkerList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.workerName = undefined
  queryParams.value.idCard = undefined
  queryParams.value.phone = undefined
  queryParams.value.workerType = undefined
  queryParams.value.dispatchCompany = undefined
  queryParams.value.bankName = undefined
  queryParams.value.bankAccount = undefined
  queryParams.value.params = {}
  handleQuery()
}

/** 字典展示辅助：标签与徽章颜色（按字典 elTagType 映射） */
function dictLabel(options, v) {
  if (v === undefined || v === null || v === '') return '-'
  const d = (options || []).find(o => String(o.value) === String(v))
  return d ? d.label : String(v)
}
function dictBadgeClass(options, v, fallback = 'gray') {
  const d = (options || []).find(o => String(o.value) === String(v))
  const map = { success: 'green', primary: 'blue', info: 'gray', warning: 'amber', danger: 'red' }
  return map[d && d.elTagType] || fallback
}

/** ===== 人员/部门/岗位弹框选择 ===== */
function openEmployeePicker() { employeePickerRef.value.open(form.value.employeeId); }
function onEmployeePickerConfirm(emp) {
  form.value.employeeId = emp.employeeId;
  form.value.employeeName = emp.employeeName;
}
function onEmployeeFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.employeeId) { form.value.employeeId = null; form.value.employeeName = ''; }
    else { openEmployeePicker(); }
    return;
  }
  openEmployeePicker();
}
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
function openPositionPicker() { positionPickerRef.value.open(form.value.positionId); }
function onPositionPickerConfirm(p) { form.value.positionId = p.positionId; form.value.positionName = p.positionName; }
function clearPosition() { form.value.positionId = null; form.value.positionName = ''; }
function onPositionFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.positionId) { clearPosition(); } else { openPositionPicker(); }
    return;
  }
  openPositionPicker();
}

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.laborId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    workerName: undefined,
    idCard: undefined,
    phone: undefined,
    workerType: undefined,
    dispatchCompany: undefined,
    bankName: undefined,
    bankAccount: undefined,
    orgId: undefined,
    orgName: undefined,
    positionId: undefined,
    positionName: undefined,
    startDate: undefined,
    endDate: undefined,
    payType: undefined,
    dailyWage: undefined,
    agreementNo: undefined,
    toRegular: undefined,
    employeeId: undefined,
    employeeName: undefined
  }
  proxy.resetForm('hrLaborWorkerRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增劳务用工'
}

function handleUpdate(row) {
  reset()
  const laborId = row.laborId || ids.value[0]
  getHrLaborWorker(laborId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改劳务用工'
  })
}

function handleView(row) {
  const laborId = row.laborId
  getHrLaborWorker(laborId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['hrLaborWorkerRef'].validate(valid => {
    if (valid) {
      if (form.value.laborId != undefined) {
        updateHrLaborWorker(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrLaborWorker(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const laborIds = row.laborId || ids.value
  proxy.$modal.confirm('是否确认删除选中的劳务用工？').then(function() {
    return delHrLaborWorker(laborIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/laborWorker/export', { ...queryParams.value }, `hrLaborWorker_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-laborworker-page {
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
.hr-laborworker-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-laborworker-page .filter-card { padding:14px 20px 16px; }
.hr-laborworker-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-laborworker-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-laborworker-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-laborworker-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-laborworker-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-laborworker-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-laborworker-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-laborworker-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-laborworker-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-laborworker-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-laborworker-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-laborworker-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-laborworker-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-laborworker-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-laborworker-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-laborworker-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-laborworker-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-laborworker-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-laborworker-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-laborworker-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-laborworker-page .field .control :deep(.el-select) { width:100%; }
.hr-laborworker-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-laborworker-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-laborworker-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-laborworker-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-laborworker-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-laborworker-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-laborworker-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-laborworker-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-laborworker-page .btn-soft .el-icon { font-size:14px; }
.hr-laborworker-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-laborworker-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-laborworker-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-laborworker-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-laborworker-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-laborworker-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-laborworker-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-laborworker-page .table-wrap { overflow-x:auto; }
.hr-laborworker-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-laborworker-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-laborworker-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-laborworker-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-laborworker-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-laborworker-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-laborworker-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-laborworker-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-laborworker-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-laborworker-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-laborworker-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-laborworker-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-laborworker-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-laborworker-page .badge.amber .dot { background:var(--amber-500); }
.hr-laborworker-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-laborworker-page .badge.blue .dot { background:var(--blue-500); }
.hr-laborworker-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-laborworker-page .badge.green .dot { background:var(--green-500); }
.hr-laborworker-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-laborworker-page .badge.red .dot { background:var(--red-500); }
.hr-laborworker-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-laborworker-page .badge.violet .dot { background:var(--brand-500); }
.hr-laborworker-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-laborworker-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-laborworker-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-laborworker-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-laborworker-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-laborworker-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-laborworker-page .pagination-container :deep(.el-pagination .btn-prev), .hr-laborworker-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-laborworker-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-laborworker-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-laborworker-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 查看详情弹窗（rd-*) ===== */
.hr-laborworker-page .rd-page {
  max-height: 60vh;
  overflow-y: auto;
}
.hr-laborworker-page .rd-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
  animation: rdFadeIn 0.4s ease-out forwards;
}
.hr-laborworker-page .rd-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f5f7fa;
  cursor: pointer;
  user-select: none;
}
.hr-laborworker-page .rd-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.hr-laborworker-page .rd-card-icon {
  display: flex;
  align-items: center;
  color: #409eff;
}
.hr-laborworker-page .rd-collapse-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  color: #909399;
  transition: transform 0.2s;
}
.hr-laborworker-page .rd-collapse-btn.is-collapsed {
  transform: rotate(-90deg);
}
.hr-laborworker-page .rd-card-body {
  padding: 16px;
}
.hr-laborworker-page .rd-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 24px;
}
.hr-laborworker-page .rd-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.hr-laborworker-page .rd-item--full {
  grid-column: 1 / -1;
}
.hr-laborworker-page .rd-label {
  font-size: 12px;
  color: #909399;
}
.hr-laborworker-page .rd-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.hr-laborworker-page .rd-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  color: #c0c4cc;
}
.hr-laborworker-page .rd-empty-icon {
  margin-bottom: 8px;
}
.hr-laborworker-page .rd-empty-text {
  font-size: 13px;
  margin: 0;
}
.hr-laborworker-page .rd-detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.hr-laborworker-page .rd-detail-header-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
}
.hr-laborworker-page .rd-detail-header-main {
  flex: 1;
}
.hr-laborworker-page .rd-detail-header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.hr-laborworker-page .rd-detail-header-sub {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}
.hr-laborworker-page .rd-detail-header-divider {
  width: 1px;
  height: 12px;
  background: #dcdfe6;
}
.hr-laborworker-page .rd-detail-header-no {
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
@media (max-width:1100px) { .hr-laborworker-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-laborworker-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-laborworker-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
