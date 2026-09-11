<template>
  <div class="app-container hr-socialsecurity-page">
    <!-- 筛选条件卡片 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
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
          <label>年度</label>
          <div class="control">
            <el-input v-model="queryParams.year" placeholder="请输入年度" clearable @keyup.enter="handleQuery">
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:socialSecurity:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:socialSecurity:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:socialSecurity:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:socialSecurity:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
          <button type="button" class="btn-soft is-outline" @click="handleBatchInit" v-hasPermi="['hr:socialSecurity:batchInit']">
            <el-icon><MagicStick /></el-icon> 批量初始化
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_socialsecurity_columns"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrSocialSecurityList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :min-width="colWidth('employeeName', 110)" show-overflow-tooltip resizable v-if="columns.employeeName.visible" />
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 120)" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo }}</span></template>
          </el-table-column>
          <el-table-column label="年度" prop="year" key="year" align="center" :width="colWidth('year', 90)" resizable v-if="columns.year.visible" />
          <el-table-column label="社保方案" prop="planId" key="planId" align="center" :width="colWidth('planId', 130)" resizable v-if="columns.planId.visible">
            <template #default="scope">{{ planName(scope.row.planId) }}</template>
          </el-table-column>
          <el-table-column label="缴费基数" prop="baseAmount" key="baseAmount" align="center" :width="colWidth('baseAmount', 130)" resizable v-if="columns.baseAmount.visible" />
          <el-table-column label="个人养老比例" prop="pensionEmployee" key="pensionEmployee" align="center" :width="colWidth('pensionEmployee', 120)" resizable v-if="columns.pensionEmployee.visible">
            <template #default="scope">{{ ratioText(scope.row, 'pensionEmployee') }}</template>
          </el-table-column>
          <el-table-column label="个人医疗比例" prop="medicalEmployee" key="medicalEmployee" align="center" :width="colWidth('medicalEmployee', 120)" resizable v-if="columns.medicalEmployee.visible">
            <template #default="scope">{{ ratioText(scope.row, 'medicalEmployee') }}</template>
          </el-table-column>
          <el-table-column label="个人公积金比例" prop="fundEmployee" key="fundEmployee" align="center" :width="colWidth('fundEmployee', 130)" resizable v-if="columns.fundEmployee.visible">
            <template #default="scope">{{ ratioText(scope.row, 'fundEmployee') }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:socialSecurity:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:socialSecurity:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:socialSecurity:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrSocialSecurityRef" :model="form" :rules="rules" label-width="130px">
        <div class="rd-page">
          <!-- 卡片：基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="姓名" prop="employeeId">
                  <el-input :model-value="form.employeeName" readonly placeholder="请选择人员">
                    <template #append>
                      <el-button v-if="form.employeeId" icon="CircleClose" @click="onEmployeeFieldClick" />
                      <el-button v-else icon="Search" @click="onEmployeeFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="部门" prop="orgName"><el-input :model-value="form.orgName" readonly placeholder="选择人员后自动带出" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工号" prop="employeeNo"><el-input v-model="form.employeeNo" placeholder="请输入工号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="年度" prop="year">
                  <el-date-picker v-model="form.year" type="year" placeholder="选择年度" value-format="YYYY" style="width: 100%" />
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 卡片：缴纳基数 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>缴纳基数</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="社保方案" prop="planId">
                  <el-select v-model="form.planId" placeholder="默认用年度方案" clearable style="width: 100%">
                    <el-option v-for="p in planOptions" :key="p.planId" :label="p.planName + '（' + p.year + (p.isDefault === 1 ? '，默认' : '') + '）'" :value="p.planId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="缴费基数" prop="baseAmount"><el-input-number v-model="form.baseAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" @change="clampBaseAmount" /></el-form-item></el-col>
              </el-row>
              <el-alert v-if="clampHint" :type="clampHint.type" :closable="false" show-icon :title="clampHint.text" style="margin-bottom: 12px" />
              <el-alert type="info" :closable="false" show-icon title="比例无需在此填写：默认继承所选方案（未选则用年度默认方案）；仅个别员工与方案不一致时，才在下方比例覆盖中单独填写。缴费基数低于方案下限/高于上限时，保存时会自动按下限/上限调整。" />
            </div>
          </section>
          <!-- 卡片：比例覆盖（可选） -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="5" x2="5" y2="19"/><circle cx="6.5" cy="6.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></svg></span>比例覆盖（仅个别员工有差异时填写，留空继承方案）</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="养老个人比例" prop="pensionEmployee">
                  <el-input-number v-model="form.pensionEmployee" :precision="2" :min="0" controls-position="right" style="width: 100%" :placeholder="planRatioPlaceholder('pensionEmployee')" />
                  <el-button v-if="form.pensionEmployee != null" link type="primary" style="margin-left:8px" @click="form.pensionEmployee = null">恢复继承</el-button>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="医疗个人比例" prop="medicalEmployee">
                  <el-input-number v-model="form.medicalEmployee" :precision="2" :min="0" controls-position="right" style="width: 100%" :placeholder="planRatioPlaceholder('medicalEmployee')" />
                  <el-button v-if="form.medicalEmployee != null" link type="primary" style="margin-left:8px" @click="form.medicalEmployee = null">恢复继承</el-button>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="失业个人比例" prop="unemploymentEmployee">
                  <el-input-number v-model="form.unemploymentEmployee" :precision="2" :min="0" controls-position="right" style="width: 100%" :placeholder="planRatioPlaceholder('unemploymentEmployee')" />
                  <el-button v-if="form.unemploymentEmployee != null" link type="primary" style="margin-left:8px" @click="form.unemploymentEmployee = null">恢复继承</el-button>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="公积金个人比例" prop="fundEmployee">
                  <el-input-number v-model="form.fundEmployee" :precision="2" :min="0" controls-position="right" style="width: 100%" :placeholder="planRatioPlaceholder('fundEmployee')" />
                  <el-button v-if="form.fundEmployee != null" link type="primary" style="margin-left:8px" @click="form.fundEmployee = null">恢复继承</el-button>
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="养老单位比例" prop="pensionEmployer">
                  <el-input-number v-model="form.pensionEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" :placeholder="planRatioPlaceholder('pensionEmployer')" />
                  <el-button v-if="form.pensionEmployer != null" link type="primary" style="margin-left:8px" @click="form.pensionEmployer = null">恢复继承</el-button>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="医疗单位比例" prop="medicalEmployer">
                  <el-input-number v-model="form.medicalEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" :placeholder="planRatioPlaceholder('medicalEmployer')" />
                  <el-button v-if="form.medicalEmployer != null" link type="primary" style="margin-left:8px" @click="form.medicalEmployer = null">恢复继承</el-button>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="失业单位比例" prop="unemploymentEmployer">
                  <el-input-number v-model="form.unemploymentEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" :placeholder="planRatioPlaceholder('unemploymentEmployer')" />
                  <el-button v-if="form.unemploymentEmployer != null" link type="primary" style="margin-left:8px" @click="form.unemploymentEmployer = null">恢复继承</el-button>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="公积金单位比例" prop="fundEmployer">
                  <el-input-number v-model="form.fundEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" :placeholder="planRatioPlaceholder('fundEmployer')" />
                  <el-button v-if="form.fundEmployer != null" link type="primary" style="margin-left:8px" @click="form.fundEmployer = null">恢复继承</el-button>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工伤单位比例" prop="injuryEmployer">
                  <el-input-number v-model="form.injuryEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" :placeholder="planRatioPlaceholder('injuryEmployer')" />
                  <el-button v-if="form.injuryEmployer != null" link type="primary" style="margin-left:8px" @click="form.injuryEmployer = null">恢复继承</el-button>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="生育单位比例" prop="maternityEmployer">
                  <el-input-number v-model="form.maternityEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" :placeholder="planRatioPlaceholder('maternityEmployer')" />
                  <el-button v-if="form.maternityEmployer != null" link type="primary" style="margin-left:8px" @click="form.maternityEmployer = null">恢复继承</el-button>
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

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">社保公积金详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.employeeNo || viewData.year">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">工号：{{ viewData.employeeNo || '-' }}</span>
              <span class="rd-detail-header-no">年度：{{ viewData.year || '-' }}</span>
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
          <div class="rd-card-body" v-show="!collapsedCards.c0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">年度</span><div class="rd-value">{{ viewData.year != null ? viewData.year : '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>缴纳基数</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">缴费基数</span><div class="rd-value">{{ viewData.baseAmount != null ? viewData.baseAmount : '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="5" x2="5" y2="19"/><circle cx="6.5" cy="6.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></svg></span>缴纳比例</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">养老单位比例</span><div class="rd-value">{{ ratioText(viewData, 'pensionEmployer') }}</div></div>
              <div class="rd-item"><span class="rd-label">养老个人比例</span><div class="rd-value">{{ ratioText(viewData, 'pensionEmployee') }}</div></div>
              <div class="rd-item"><span class="rd-label">医疗单位比例</span><div class="rd-value">{{ ratioText(viewData, 'medicalEmployer') }}</div></div>
              <div class="rd-item"><span class="rd-label">医疗个人比例</span><div class="rd-value">{{ ratioText(viewData, 'medicalEmployee') }}</div></div>
              <div class="rd-item"><span class="rd-label">失业单位比例</span><div class="rd-value">{{ ratioText(viewData, 'unemploymentEmployer') }}</div></div>
              <div class="rd-item"><span class="rd-label">失业个人比例</span><div class="rd-value">{{ ratioText(viewData, 'unemploymentEmployee') }}</div></div>
              <div class="rd-item"><span class="rd-label">工伤单位比例</span><div class="rd-value">{{ ratioText(viewData, 'injuryEmployer') }}</div></div>
              <div class="rd-item"><span class="rd-label">生育单位比例</span><div class="rd-value">{{ ratioText(viewData, 'maternityEmployer') }}</div></div>
              <div class="rd-item"><span class="rd-label">公积金单位比例</span><div class="rd-value">{{ ratioText(viewData, 'fundEmployer') }}</div></div>
              <div class="rd-item"><span class="rd-label">公积金个人比例</span><div class="rd-value">{{ ratioText(viewData, 'fundEmployee') }}</div></div>
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

    <!-- ===== 批量初始化年度选择弹窗 ===== -->
    <el-dialog v-model="batchInitOpen" width="420px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon><MagicStick /></el-icon></div>
          <span class="rd-detail-header-title">批量初始化</span>
        </div>
      </template>
      <div class="rd-page">
        <el-form label-width="90px">
          <el-form-item label="年度">
            <el-date-picker v-model="batchInitYear" type="year" placeholder="选择年度" value-format="YYYY" style="width: 100%" />
          </el-form-item>
        </el-form>
        <el-alert type="info" :closable="false" show-icon title="将按该年度默认方案，为尚未配置此年度社保的在职员工批量生成记录（基数取方案下限）。" />
      </div>
      <template #footer>
        <el-button type="primary" :disabled="!batchInitYear" @click="doBatchInit">初始化</el-button>
        <el-button @click="batchInitOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HrSocialSecurity">
import { listHrSocialSecurity, getHrSocialSecurity, addHrSocialSecurity, updateHrSocialSecurity, delHrSocialSecurity } from '@/api/hr/hrSocialSecurity'
import { listSsPlan, batchInitSs } from '@/api/hr/ssPlan'
import { useColumnResize } from '@/composables/useColumnResize'
import EmployeePicker from '@/components/EmployeePicker'

const { proxy } = getCurrentInstance()

const hrSocialSecurityList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const collapsedCards = reactive({ c0: false, c1: false, c2: false })

/** 列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_socialsecurity_index')

/** 列显隐配置 */
const defaultColumns = {
  employeeName: { label: '员工姓名', visible: true },
  employeeNo: { label: '工号', visible: true },
  year: { label: '年度', visible: true },
  planId: { label: '社保方案', visible: true },
  baseAmount: { label: '缴费基数', visible: true },
  pensionEmployee: { label: '个人养老比例', visible: true },
  medicalEmployee: { label: '个人医疗比例', visible: true },
  fundEmployee: { label: '个人公积金比例', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_socialsecurity_columns')
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

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.employeeName) count++
  if (queryParams.value.employeeNo) count++
  if (queryParams.value.year) count++
  return count
})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, employeeId: undefined, employeeName: undefined, employeeNo: undefined, year: undefined },
  rules: {
    employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }],
    year: [{ required: true, message: '年度不能为空', trigger: 'blur' }],
    baseAmount: [{ required: true, message: '缴费基数不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrSocialSecurity(queryParams.value).then(response => {
    hrSocialSecurityList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  queryParams.value.year = undefined
  queryParams.value.params = {}
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.ssId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function reset() {
  form.value = {
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    orgName: undefined,
    year: undefined,
    baseAmount: undefined,
    planId: undefined,
    pensionEmployer: undefined,
    pensionEmployee: undefined,
    medicalEmployer: undefined,
    medicalEmployee: undefined,
    unemploymentEmployer: undefined,
    unemploymentEmployee: undefined,
    injuryEmployer: undefined,
    maternityEmployer: undefined,
    fundEmployer: undefined,
    fundEmployee: undefined
  }
  proxy.resetForm('hrSocialSecurityRef')
}

/** ===== 社保方案支持 ===== */
const planOptions = ref([])
const planMap = ref({})
function loadPlans() {
  listSsPlan({ pageNum: 1, pageSize: 100, status: '0' }).then(res => {
    planOptions.value = res.rows || []
    const m = {}
    planOptions.value.forEach(p => { m[p.planId] = p })
    planMap.value = m
  }).catch(() => {})
}
function planName(planId) {
  const p = planMap.value[planId]
  if (!p) return planId ? '方案#' + planId : '-'
  return p.planName + (p.isDefault === 1 ? '（默认）' : '')
}
/** 比例展示：员工覆盖值优先，否则显示所属方案比例 */
function ratioText(row, key) {
  const own = row[key]
  const pct = v => v != null ? (Number(v) * 100).toFixed(2).replace(/\.00$/, '') + '%' : null
  if (own != null) return pct(own)
  const p = planMap.value[row.planId]
  if (p && p[key] != null) return pct(p[key]) + '（方案）'
  return '-'
}
/** 批量初始化：年度选择弹窗 */
const batchInitOpen = ref(false)
const batchInitYear = ref(String(new Date().getFullYear()))
function handleBatchInit() {
  batchInitYear.value = String(new Date().getFullYear())
  batchInitOpen.value = true
}
function doBatchInit() {
  batchInitSs(batchInitYear.value).then(res => {
    proxy.$modal.msgSuccess(res.msg || '初始化完成')
    batchInitOpen.value = false
    getList()
  }).catch(() => {})
}

/** 年度变化时联动社保方案：当前所选方案不属于新年份时，切换为新年份默认方案 */
watch(() => form.value.year, (y) => {
  if (!y) return
  const yearNum = Number(y)
  const cur = planOptions.value.find(p => p.planId === form.value.planId)
  if (!cur || cur.year !== yearNum) {
    const def = planOptions.value.find(p => p.isDefault === 1 && p.year === yearNum)
    form.value.planId = def ? def.planId : undefined
  }
  // 基数钳制提示失效时清除
  clampHint.value = null
})

/** 当前生效方案（员工所选 > 年度默认） */
function effectivePlan() {
  const byId = form.value.planId ? planOptions.value.find(p => p.planId === form.value.planId) : null
  if (byId) return byId
  const yearNum = form.value.year ? Number(form.value.year) : null
  return planOptions.value.find(p => p.isDefault === 1 && p.year === yearNum) || null
}
/** 比例输入框 placeholder：显示生效方案的比例（百分数），空方案提示继承 */
function planRatioPlaceholder(key) {
  const p = effectivePlan()
  if (p && p[key] != null) return '方案值 ' + (Number(p[key]) * 100).toFixed(2).replace(/\.00$/, '') + '%'
  return '留空继承方案'
}

/** 基数钳制：录入超出所选方案（或年度默认方案）上下限时即时校正并提示 */
const clampHint = ref(null)
function clampBaseAmount() {
  const v = form.value.baseAmount
  if (v == null) { clampHint.value = null; return }
  let plan = form.value.planId ? planOptions.value.find(p => p.planId === form.value.planId) : null
  if (plan && plan.year !== form.value.year) plan = null
  if (!plan) plan = planOptions.value.find(p => p.isDefault === 1 && p.year === form.value.year)
  if (!plan) { clampHint.value = null; return }
  if (plan.baseLower != null && v < plan.baseLower) {
    form.value.baseAmount = Number(plan.baseLower)
    clampHint.value = { type: 'warning', text: `基数低于方案「${plan.planName}」下限，已按 ${plan.baseLower} 调整` }
  } else if (plan.baseUpper != null && v > plan.baseUpper) {
    form.value.baseAmount = Number(plan.baseUpper)
    clampHint.value = { type: 'warning', text: `基数高于方案「${plan.planName}」上限，已按 ${plan.baseUpper} 调整` }
  } else {
    clampHint.value = null
  }
}

function handleAdd() {
  reset()
  form.value.year = String(new Date().getFullYear())
  // 社保方案默认取该年度默认方案
  const defaultPlan = planOptions.value.find(p => p.isDefault === 1 && p.year === Number(form.value.year))
  if (defaultPlan) form.value.planId = defaultPlan.planId
  open.value = true
  title.value = '新增社保公积金'
}

function handleUpdate(row) {
  reset()
  const ssId = row.ssId || ids.value[0]
  getHrSocialSecurity(ssId).then(response => {
    form.value = response.data
    // 年度选择器需要字符串
    if (form.value.year != null) form.value.year = String(form.value.year)
    open.value = true
    title.value = '修改社保公积金'
  })
}

function handleView(row) {
  const ssId = row.ssId
  getHrSocialSecurity(ssId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['hrSocialSecurityRef'].validate(valid => {
    if (valid) {
      // 年度选择器返回字符串，提交前转为数字
      const payload = { ...form.value }
      if (payload.year != null) payload.year = Number(payload.year)
      if (payload.ssId != undefined) {
        updateHrSocialSecurity(payload).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrSocialSecurity(payload).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const ssIds = row.ssId || ids.value
  proxy.$modal.confirm('是否确认删除选中的社保公积金？').then(function() {
    return delHrSocialSecurity(ssIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/socialSecurity/export', proxy.withExportIds({ ...queryParams.value }, ids.value, 'ssId'), `hrSocialSecurity_${new Date().getTime()}.xlsx`)
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

loadPlans()
getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-socialsecurity-page {
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
.hr-socialsecurity-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-socialsecurity-page .filter-card { padding:14px 20px 16px; }
.hr-socialsecurity-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-socialsecurity-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-socialsecurity-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-socialsecurity-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-socialsecurity-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-socialsecurity-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-socialsecurity-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-socialsecurity-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-socialsecurity-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-socialsecurity-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-socialsecurity-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-socialsecurity-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-socialsecurity-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-socialsecurity-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-socialsecurity-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-socialsecurity-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-socialsecurity-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-socialsecurity-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-socialsecurity-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-socialsecurity-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-socialsecurity-page .field .control :deep(.el-select) { width:100%; }
.hr-socialsecurity-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-socialsecurity-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-socialsecurity-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-socialsecurity-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-socialsecurity-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-socialsecurity-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-socialsecurity-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-socialsecurity-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-socialsecurity-page .btn-soft .el-icon { font-size:14px; }
.hr-socialsecurity-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-socialsecurity-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-socialsecurity-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-socialsecurity-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-socialsecurity-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-socialsecurity-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-socialsecurity-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-socialsecurity-page .table-wrap { overflow-x:auto; }
.hr-socialsecurity-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-socialsecurity-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-socialsecurity-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-socialsecurity-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-socialsecurity-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-socialsecurity-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-socialsecurity-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-socialsecurity-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-socialsecurity-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-socialsecurity-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-socialsecurity-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-socialsecurity-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-socialsecurity-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-socialsecurity-page .badge.amber .dot { background:var(--amber-500); }
.hr-socialsecurity-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-socialsecurity-page .badge.blue .dot { background:var(--blue-500); }
.hr-socialsecurity-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-socialsecurity-page .badge.green .dot { background:var(--green-500); }
.hr-socialsecurity-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-socialsecurity-page .badge.red .dot { background:var(--red-500); }
.hr-socialsecurity-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-socialsecurity-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-socialsecurity-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-socialsecurity-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-socialsecurity-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-socialsecurity-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-socialsecurity-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-socialsecurity-page .pagination-container :deep(.el-pagination .btn-prev), .hr-socialsecurity-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-socialsecurity-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-socialsecurity-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-socialsecurity-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 详情弹窗样式（遵循 detail-page-style-guide 规范） ===== */
.hr-socialsecurity-page .rd-page { max-height: 62vh; overflow-y: auto; }
.hr-socialsecurity-page .rd-card { border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 16px; overflow: hidden; }
.hr-socialsecurity-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: #f5f7fa; cursor: pointer; user-select: none; }
.hr-socialsecurity-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #303133; }
.hr-socialsecurity-page .rd-card-icon { display: flex; align-items: center; color: #409eff; }
.hr-socialsecurity-page .rd-collapse-btn { border: none; background: transparent; cursor: pointer; padding: 4px; display: flex; align-items: center; color: #909399; transition: transform 0.2s; }
.hr-socialsecurity-page .rd-collapse-btn.is-collapsed { transform: rotate(-90deg); }
.hr-socialsecurity-page .rd-card-body { padding: 16px; }
.hr-socialsecurity-page .rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.hr-socialsecurity-page .rd-item { display: flex; flex-direction: column; gap: 4px; }
.hr-socialsecurity-page .rd-item--full { grid-column: 1 / -1; }
.hr-socialsecurity-page .rd-label { font-size: 12px; color: #909399; }
.hr-socialsecurity-page .rd-value { font-size: 14px; color: #303133; font-weight: 500; }
.hr-socialsecurity-page .rd-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 32px; color: #c0c4cc; }
.hr-socialsecurity-page .rd-empty-icon { margin-bottom: 8px; }
.hr-socialsecurity-page .rd-empty-text { font-size: 13px; margin: 0; }
.hr-socialsecurity-page .rd-detail-header { display: flex; align-items: center; gap: 12px; }
.hr-socialsecurity-page .rd-detail-header-icon { width: 36px; height: 36px; border-radius: 8px; background: #ecf5ff; display: flex; align-items: center; justify-content: center; color: #409eff; }
.hr-socialsecurity-page .rd-detail-header-main { flex: 1; }
.hr-socialsecurity-page .rd-detail-header-title { font-size: 16px; font-weight: 600; color: #303133; }
.hr-socialsecurity-page .rd-detail-header-sub { display: flex; align-items: center; gap: 12px; margin-top: 4px; }
.hr-socialsecurity-page .rd-detail-header-divider { width: 1px; height: 12px; background: #dcdfe6; }
.hr-socialsecurity-page .rd-detail-header-no { font-size: 13px; color: #909399; }
@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.hr-socialsecurity-page .rd-card { animation: rdFadeIn 0.4s ease-out forwards; }
.hr-socialsecurity-page .rd-card:nth-child(2) { animation-delay: 0.06s; }
.hr-socialsecurity-page .rd-card:nth-child(3) { animation-delay: 0.12s; }
.hr-socialsecurity-page .rd-card:nth-child(4) { animation-delay: 0.18s; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-socialsecurity-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-socialsecurity-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-socialsecurity-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
