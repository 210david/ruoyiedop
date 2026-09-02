<template>
  <div class="app-container hr-employee-page">
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
          <label>工号</label>
          <div class="control">
            <el-input v-model="queryParams.employeeNo" placeholder="请输入工号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>姓名</label>
          <div class="control">
            <el-input v-model="queryParams.employeeName" placeholder="请输入姓名" clearable @keyup.enter="handleQuery">
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
          <label>员工状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.employeeStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in hr_employee_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>性别</label>
          <div class="control is-select">
            <el-select v-model="queryParams.gender" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in hr_gender" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>所属部门</label>
          <div class="control is-select">
            <el-tree-select v-model="queryParams.orgId" :data="deptOptions" :props="{ value: 'id', label: 'label', children: 'children' }" value-key="id" placeholder="请选择部门" check-strictly clearable style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>岗位名称</label>
          <div class="control">
            <el-input v-model="queryParams.positionName" placeholder="请输入岗位名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>联系电话</label>
          <div class="control">
            <el-input v-model="queryParams.phone" placeholder="请输入联系电话" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>邮箱</label>
          <div class="control">
            <el-input v-model="queryParams.email" placeholder="请输入邮箱" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>学历</label>
          <div class="control is-select">
            <el-select v-model="queryParams.eduLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in hr_edu_level" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>用工类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.employeeType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in hr_employee_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>入职日期</label>
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
      <!-- 状态页签 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button type="button" class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button v-for="d in hr_employee_status" :key="d.value" type="button" class="status-tab" :class="[statusTabClass(d.value), { 'is-active': activeStatusTab === d.value }]" @click="handleStatusTabClick(d.value)">
            <span class="dot"></span><span>{{ d.label }}</span><span class="count">{{ statusCounts[d.value] || 0 }}</span>
          </button>
        </div>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:employee:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:employee:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:employee:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:employee:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
          <button type="button" class="btn-soft is-outline" @click="handleImport" v-hasPermi="['hr:employee:import']">
            <el-icon><Upload /></el-icon> 导入
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_employee_columns"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrEmployeeList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" :width="colWidth('序号', 85)" align="center" />
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 120)" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo }}</span></template>
          </el-table-column>
          <el-table-column label="姓名" prop="employeeName" key="employeeName" align="center" :width="colWidth('employeeName', 100)" resizable v-if="columns.employeeName.visible" />
          <el-table-column label="性别" prop="gender" key="gender" align="center" :width="colWidth('gender', 80)" resizable v-if="columns.gender.visible">
            <template #default="scope">
              <span class="badge blue" v-if="scope.row.gender === '0'">男</span>
              <span class="badge violet" v-else-if="scope.row.gender === '1'">女</span>
              <span v-else>{{ scope.row.gender || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="岗位" prop="positionName" key="positionName" align="center" :width="colWidth('positionName', 140)" resizable show-overflow-tooltip v-if="columns.positionName.visible" />
          <el-table-column label="身份证号" prop="idCard" key="idCard" align="center" :width="colWidth('idCard', 180)" resizable show-overflow-tooltip v-if="columns.idCard.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.idCard }}</span></template>
          </el-table-column>
          <el-table-column label="联系电话" prop="phone" key="phone" align="center" :width="colWidth('phone', 120)" resizable v-if="columns.phone.visible" />
          <el-table-column label="入职日期" prop="entryDate" key="entryDate" align="center" :width="colWidth('entryDate', 120)" resizable v-if="columns.entryDate.visible" />
          <el-table-column label="状态" prop="employeeStatus" key="employeeStatus" align="center" :width="colWidth('employeeStatus', 100)" resizable v-if="columns.employeeStatus.visible">
            <template #default="scope">
              <span class="badge green" v-if="scope.row.employeeStatus === '0'">{{ statusLabel(scope.row.employeeStatus) }}</span>
              <span class="badge red" v-else-if="scope.row.employeeStatus === '2'">{{ statusLabel(scope.row.employeeStatus) }}</span>
              <span class="badge amber" v-else>{{ scope.row.employeeStatus ? statusLabel(scope.row.employeeStatus) : '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:employee:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:employee:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:employee:remove']">删除</el-button>
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
    <el-dialog v-model="open" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrEmployeeRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 关键信息 / 完整信息 维护切换 -->
          <div class="full-toggle-bar">
            <span class="full-toggle-tip">{{ showFullInfo ? '当前为完整信息维护模式，可维护全部信息及明细' : '默认仅维护关键信息，开启「完整信息维护」后可维护个人信息、工作信息及家庭成员等明细' }}</span>
            <el-switch v-model="showFullInfo" active-text="完整信息维护" />
          </div>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="工号" prop="employeeNo"><el-input v-model="form.employeeNo" placeholder="自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="姓名" prop="employeeName"><el-input v-model="form.employeeName" placeholder="请输入姓名" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="身份证号" prop="idCard"><el-input v-model="form.idCard" placeholder="请输入18位身份证号" maxlength="18" clearable @input="onIdCardInput" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="所属部门" prop="orgId">
                  <el-input :model-value="form.orgName" readonly placeholder="请选择部门" @click="onOrgFieldClick">
                    <template #append>
                      <el-button v-if="form.orgId" icon="CircleClose" @click="onOrgFieldClick" />
                      <el-button v-else icon="Search" @click="onOrgFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="岗位" prop="positionName">
                  <el-input :model-value="form.positionName" readonly placeholder="请选择岗位" @click="onPositionFieldClick">
                    <template #append>
                      <el-button v-if="form.positionId" icon="CircleClose" @click="onPositionFieldClick" />
                      <el-button v-else icon="Search" @click="onPositionFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="联系电话" prop="phone"><el-input v-model="form.phone" placeholder="请输入联系电话" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="邮箱" prop="email"><el-input v-model="form.email" placeholder="请输入邮箱" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="员工状态" prop="employeeStatus"><el-select v-model="form.employeeStatus" placeholder="请选择" style="width: 100%"><el-option v-for="d in hr_employee_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="照片" prop="photo"><image-upload v-model="form.photo" :limit="1" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('s0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg></span>系统信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.s0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.s0">
              <template v-if="!form.userId">
                <el-row :gutter="20">
                  <el-col :span="12"><el-form-item label="登录账号" prop="userName"><el-input v-model="form.userName" placeholder="不填则不同步创建用户账号" maxlength="30" clearable /></el-form-item></el-col>
                  <el-col :span="12"><el-form-item label="角色" prop="roleIds">
                    <el-input :model-value="roleNames" readonly placeholder="请选择角色（可多选）" @click="onRoleFieldClick">
                      <template #append>
                        <el-button v-if="form.roleIds && form.roleIds.length" icon="CircleClose" @click="onRoleFieldClick" />
                        <el-button v-else icon="Search" @click="onRoleFieldClick" />
                      </template>
                    </el-input>
                  </el-form-item></el-col>
                  <el-col :span="12"><el-form-item label="初始密码" prop="password"><el-input v-model="form.password" type="password" show-password placeholder="填写账号时必填" maxlength="20" autocomplete="new-password" /></el-form-item></el-col>
                  <el-col :span="12"><el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入初始密码" maxlength="20" autocomplete="new-password" /></el-form-item></el-col>
                </el-row>
              </template>
              <el-alert v-else type="info" :closable="false" show-icon :title="'该员工已关联系统账号「' + form.userNickName + '」，账号信息请前往 系统管理-用户管理 维护'" />
            </div>
          </section>
          <template v-if="showFullInfo">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>个人信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="性别" prop="gender"><el-select v-model="form.gender" placeholder="可根据身份证号自动识别" clearable style="width: 100%"><el-option v-for="d in hr_gender" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="出生日期" prop="birthday"><el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="可根据身份证号自动识别" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="民族" prop="nation"><el-input v-model="form.nation" placeholder="请输入民族" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="政治面貌" prop="politicsStatus"><el-select v-model="form.politicsStatus" placeholder="请选择" style="width: 100%"><el-option label="群众" value="群众" /><el-option label="党员" value="党员" /><el-option label="团员" value="团员" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="婚姻状况" prop="maritalStatus"><el-select v-model="form.maritalStatus" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in hr_marital_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="籍贯" prop="nativePlace"><el-input v-model="form.nativePlace" placeholder="请输入籍贯" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="现住址" prop="address"><el-input v-model="form.address" placeholder="请输入现住址" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="紧急联系人" prop="emergencyContact"><el-input v-model="form.emergencyContact" placeholder="请输入紧急联系人" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="紧急电话" prop="emergencyPhone"><el-input v-model="form.emergencyPhone" placeholder="请输入紧急电话" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="学历" prop="eduLevel"><el-select v-model="form.eduLevel" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in hr_edu_level" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="毕业院校" prop="graduationSchool"><el-input v-model="form.graduationSchool" placeholder="请输入毕业院校" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="专业" prop="major"><el-input v-model="form.major" placeholder="请输入专业" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="毕业日期" prop="graduationDate"><el-date-picker v-model="form.graduationDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>工作信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="入职日期" prop="entryDate"><el-date-picker v-model="form.entryDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="转正日期" prop="regularDate"><el-date-picker v-model="form.regularDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="离职日期" prop="leaveDate"><el-date-picker v-model="form.leaveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="离职原因" prop="leaveReason"><el-input v-model="form.leaveReason" placeholder="请输入离职原因" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="用工类型" prop="employeeType"><el-select v-model="form.employeeType" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in hr_employee_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工时制度" prop="workType"><el-select v-model="form.workType" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in hr_work_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="技能等级" prop="skillLevel"><el-input v-model="form.skillLevel" placeholder="请输入技能等级" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="社会工龄" prop="socialWorkYears"><el-input-number v-model="form.socialWorkYears" :precision="0" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="本公司工龄" prop="companyWorkYears"><el-input-number v-model="form.companyWorkYears" :precision="0" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="开户银行" prop="bankName"><el-input v-model="form.bankName" placeholder="请输入开户银行" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="银行卡号" prop="bankAccount"><el-input v-model="form.bankAccount" placeholder="请输入银行卡号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="社保号" prop="socialSecurityNo"><el-input v-model="form.socialSecurityNo" placeholder="请输入社保号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="公积金号" prop="houseFundNo"><el-input v-model="form.houseFundNo" placeholder="请输入公积金号" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- ===== 明细表：家庭成员 ===== -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>家庭成员</div>
              <el-button type="primary" link icon="Plus" @click.stop="addFamilyRow">添加</el-button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d0">
              <el-table :data="form.familyList" border size="small">
                <el-table-column label="姓名" width="120"><template #default="s"><el-input v-model="s.row.memberName" placeholder="姓名" /></template></el-table-column>
                <el-table-column label="关系" width="110"><template #default="s"><el-input v-model="s.row.relationship" placeholder="如：父子" /></template></el-table-column>
                <el-table-column label="性别" width="90"><template #default="s"><el-select v-model="s.row.gender" placeholder="选择" clearable><el-option v-for="d in hr_gender" :key="d.value" :label="d.label" :value="d.value" /></el-select></template></el-table-column>
                <el-table-column label="出生日期" width="150"><template #default="s"><el-date-picker v-model="s.row.birthday" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></template></el-table-column>
                <el-table-column label="联系电话" width="140"><template #default="s"><el-input v-model="s.row.phone" /></template></el-table-column>
                <el-table-column label="工作单位/职业" min-width="150"><template #default="s"><el-input v-model="s.row.occupation" /></template></el-table-column>
                <el-table-column label="操作" width="70" align="center"><template #default="s"><el-button link type="danger" icon="Delete" @click="form.familyList.splice(s.$index, 1)" /></template></el-table-column>
              </el-table>
            </div>
          </section>

          <!-- ===== 明细表：教育背景 ===== -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg></span>教育背景</div>
              <el-button type="primary" link icon="Plus" @click.stop="addEducationRow">添加</el-button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d1">
              <el-table :data="form.educationList" border size="small">
                <el-table-column label="开始日期" width="150"><template #default="s"><el-date-picker v-model="s.row.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></template></el-table-column>
                <el-table-column label="结束日期" width="150"><template #default="s"><el-date-picker v-model="s.row.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></template></el-table-column>
                <el-table-column label="学校名称" min-width="140"><template #default="s"><el-input v-model="s.row.schoolName" /></template></el-table-column>
                <el-table-column label="专业" min-width="110"><template #default="s"><el-input v-model="s.row.major" /></template></el-table-column>
                <el-table-column label="学历" width="120"><template #default="s"><el-select v-model="s.row.eduLevel" placeholder="选择" clearable><el-option v-for="d in hr_edu_level" :key="d.value" :label="d.label" :value="d.value" /></el-select></template></el-table-column>
                <el-table-column label="学位" width="100"><template #default="s"><el-input v-model="s.row.degree" /></template></el-table-column>
                <el-table-column label="最高学历" width="90"><template #default="s"><el-select v-model="s.row.isHighest" placeholder="选择"><el-option label="是" value="1" /><el-option label="否" value="0" /></el-select></template></el-table-column>
                <el-table-column label="操作" width="70" align="center"><template #default="s"><el-button link type="danger" icon="Delete" @click="form.educationList.splice(s.$index, 1)" /></template></el-table-column>
              </el-table>
            </div>
          </section>

          <!-- ===== 明细表：工作经历 ===== -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>工作经历</div>
              <el-button type="primary" link icon="Plus" @click.stop="addWorkExpRow">添加</el-button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d2">
              <el-table :data="form.workExpList" border size="small">
                <el-table-column label="开始日期" width="150"><template #default="s"><el-date-picker v-model="s.row.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></template></el-table-column>
                <el-table-column label="结束日期" width="150"><template #default="s"><el-date-picker v-model="s.row.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></template></el-table-column>
                <el-table-column label="工作单位" min-width="140"><template #default="s"><el-input v-model="s.row.companyName" /></template></el-table-column>
                <el-table-column label="职位" min-width="100"><template #default="s"><el-input v-model="s.row.position" /></template></el-table-column>
                <el-table-column label="工作内容" min-width="140"><template #default="s"><el-input v-model="s.row.workContent" /></template></el-table-column>
                <el-table-column label="离职原因" min-width="110"><template #default="s"><el-input v-model="s.row.leaveReason" /></template></el-table-column>
                <el-table-column label="操作" width="70" align="center"><template #default="s"><el-button link type="danger" icon="Delete" @click="form.workExpList.splice(s.$index, 1)" /></template></el-table-column>
              </el-table>
            </div>
          </section>

          <!-- ===== 明细表：培训/职称/资格认证 ===== -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>培训/职称/资格认证</div>
              <el-button type="primary" link icon="Plus" @click.stop="addQualificationRow">添加</el-button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d3">
              <el-table :data="form.qualificationList" border size="small">
                <el-table-column label="名称" min-width="130"><template #default="s"><el-input v-model="s.row.qualName" /></template></el-table-column>
                <el-table-column label="证书编号" width="120"><template #default="s"><el-input v-model="s.row.qualNo" /></template></el-table-column>
                <el-table-column label="发证机构" min-width="120"><template #default="s"><el-input v-model="s.row.issueOrg" /></template></el-table-column>
                <el-table-column label="发证日期" width="150"><template #default="s"><el-date-picker v-model="s.row.issueDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></template></el-table-column>
                <el-table-column label="有效期从" width="150"><template #default="s"><el-date-picker v-model="s.row.validFrom" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></template></el-table-column>
                <el-table-column label="有效期至" width="150"><template #default="s"><el-date-picker v-model="s.row.validTo" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></template></el-table-column>
                <el-table-column label="成绩/等级" width="100"><template #default="s"><el-input v-model="s.row.score" /></template></el-table-column>
                <el-table-column label="操作" width="70" align="center"><template #default="s"><el-button link type="danger" icon="Delete" @click="form.qualificationList.splice(s.$index, 1)" /></template></el-table-column>
              </el-table>
            </div>
          </section>
          </template>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看详情弹窗（三页签卡片式布局） ===== -->
    <el-dialog v-model="viewOpen" width="1152px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">员工详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.employeeNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">工号：{{ viewData.employeeNo }}</span>
              <span class="rd-detail-header-no">姓名：{{ viewData.employeeName }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <el-tabs v-model="activeTab" class="detail-tabs">
          <!-- ===== 基本信息 ===== -->
          <el-tab-pane label="基本信息" name="basic">
            <section class="rd-card">
              <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>核心信息</div></div>
              <div class="rd-card-body" style="display:block">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">岗位</span><div class="rd-value">{{ viewData.positionName || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">部门</span><div class="rd-value">{{ viewData.orgName || '-' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 11V7a4 4 0 0 0-8 0V11"/><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/></svg></span>联系信息</div></div>
              <div class="rd-card-body" style="display:block">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">联系电话</span><div class="rd-value">{{ viewData.phone || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">邮箱</span><div class="rd-value">{{ viewData.email || '-' }}</div></div>
                </div>
              </div>
            </section>
          </el-tab-pane>

          <!-- ===== 个人信息 ===== -->
          <el-tab-pane label="个人信息" name="personal">
            <section class="rd-card">
              <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>个人资料</div></div>
              <div class="rd-card-body" style="display:block">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">身份证号</span><div class="rd-value">{{ viewData.idCard || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">性别</span><div class="rd-value">{{ dictLabel(hr_gender, viewData.gender) }}</div></div>
                  <div class="rd-item"><span class="rd-label">出生日期</span><div class="rd-value">{{ viewData.birthday || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">民族</span><div class="rd-value">{{ viewData.nation || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">政治面貌</span><div class="rd-value">{{ viewData.politicsStatus || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">婚姻状况</span><div class="rd-value">{{ dictLabel(hr_marital_status, viewData.maritalStatus) }}</div></div>
                  <div class="rd-item"><span class="rd-label">籍贯</span><div class="rd-value">{{ viewData.nativePlace || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">紧急联系人</span><div class="rd-value">{{ viewData.emergencyContact || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">紧急电话</span><div class="rd-value">{{ viewData.emergencyPhone || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">现住址</span><div class="rd-value">{{ viewData.address || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">学历</span><div class="rd-value">{{ dictLabel(hr_edu_level, viewData.eduLevel) }}</div></div>
                  <div class="rd-item"><span class="rd-label">毕业院校</span><div class="rd-value">{{ viewData.graduationSchool || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">专业</span><div class="rd-value">{{ viewData.major || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">毕业日期</span><div class="rd-value">{{ viewData.graduationDate || '-' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>家庭成员</div></div>
              <div class="rd-card-body" style="display:block">
                <el-table :data="viewData.familyList || []" border size="small" v-if="viewData.familyList && viewData.familyList.length">
                  <el-table-column label="序号" type="index" :width="colWidth('序号', 85)" align="center" />
                  <el-table-column label="姓名" prop="memberName" align="center" min-width="100" />
                  <el-table-column label="关系" prop="relationship" align="center" width="100" />
                  <el-table-column label="性别" prop="gender" align="center" width="80">
                    <template #default="scope">{{ dictLabel(hr_gender, scope.row.gender) }}</template>
                  </el-table-column>
                  <el-table-column label="出生日期" prop="birthday" align="center" width="120" />
                  <el-table-column label="联系电话" prop="phone" align="center" width="120" />
                  <el-table-column label="工作单位/职业" prop="occupation" align="center" min-width="150" show-overflow-tooltip />
                </el-table>
                <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg><p class="rd-empty-text">暂无家庭成员信息</p></div>
              </div>
            </section>
          </el-tab-pane>

          <!-- ===== 工作信息 ===== -->
          <el-tab-pane label="工作信息" name="work">
            <section class="rd-card">
              <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>工作信息</div></div>
              <div class="rd-card-body" style="display:block">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">入职日期</span><div class="rd-value">{{ viewData.entryDate || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">转正日期</span><div class="rd-value">{{ viewData.regularDate || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">离职日期</span><div class="rd-value">{{ viewData.leaveDate || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">员工状态</span><div class="rd-value">{{ statusLabel(viewData.employeeStatus) }}</div></div>
                  <div class="rd-item"><span class="rd-label">用工类型</span><div class="rd-value">{{ dictLabel(hr_employee_type, viewData.employeeType) }}</div></div>
                  <div class="rd-item"><span class="rd-label">工时制度</span><div class="rd-value">{{ dictLabel(hr_work_type, viewData.workType) }}</div></div>
                  <div class="rd-item"><span class="rd-label">技能等级</span><div class="rd-value">{{ viewData.skillLevel || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">社会工龄</span><div class="rd-value">{{ viewData.socialWorkYears != null ? viewData.socialWorkYears + ' 年' : '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">本公司工龄</span><div class="rd-value">{{ viewData.companyWorkYears != null ? viewData.companyWorkYears + ' 年' : '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">离职原因</span><div class="rd-value">{{ viewData.leaveReason || '-' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/></svg></span>银行与社保</div></div>
              <div class="rd-card-body" style="display:block">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">开户银行</span><div class="rd-value">{{ viewData.bankName || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">银行卡号</span><div class="rd-value">{{ viewData.bankAccount || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">社保号</span><div class="rd-value">{{ viewData.socialSecurityNo || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">参保状态</span><div class="rd-value">{{ dictLabel(hr_social_security_status, viewData.socialSecurityStatus) }}</div></div>
                  <div class="rd-item"><span class="rd-label">公积金号</span><div class="rd-value">{{ viewData.houseFundNo || '-' }}</div></div>
                </div>
              </div>
            </section>

            <section class="rd-card">
              <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg></span>教育背景</div></div>
              <div class="rd-card-body" style="display:block">
                <el-table :data="viewData.educationList || []" border size="small" v-if="viewData.educationList && viewData.educationList.length">
                  <el-table-column label="序号" type="index" :width="colWidth('序号', 85)" align="center" />
                  <el-table-column label="开始日期" prop="startDate" align="center" width="120" />
                  <el-table-column label="结束日期" prop="endDate" align="center" width="120" />
                  <el-table-column label="学校名称" prop="schoolName" align="center" min-width="150" show-overflow-tooltip />
                  <el-table-column label="专业" prop="major" align="center" min-width="100" show-overflow-tooltip />
                  <el-table-column label="学历" prop="eduLevel" align="center" width="100">
                    <template #default="scope">{{ dictLabel(hr_edu_level, scope.row.eduLevel) }}</template>
                  </el-table-column>
                  <el-table-column label="学位" prop="degree" align="center" width="100" />
                  <el-table-column label="最高学历" prop="isHighest" align="center" width="100">
                    <template #default="scope">{{ scope.row.isHighest === '1' ? '是' : '否' }}</template>
                  </el-table-column>
                </el-table>
                <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg><p class="rd-empty-text">暂无教育背景信息</p></div>
              </div>
            </section>

            <section class="rd-card">
              <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>工作经历</div></div>
              <div class="rd-card-body" style="display:block">
                <el-table :data="viewData.workExpList || []" border size="small" v-if="viewData.workExpList && viewData.workExpList.length">
                  <el-table-column label="序号" type="index" :width="colWidth('序号', 85)" align="center" />
                  <el-table-column label="开始日期" prop="startDate" align="center" width="120" />
                  <el-table-column label="结束日期" prop="endDate" align="center" width="120" />
                  <el-table-column label="工作单位" prop="companyName" align="center" min-width="150" show-overflow-tooltip />
                  <el-table-column label="职位" prop="position" align="center" min-width="100" show-overflow-tooltip />
                  <el-table-column label="工作内容" prop="workContent" align="center" min-width="150" show-overflow-tooltip />
                  <el-table-column label="离职原因" prop="leaveReason" align="center" min-width="120" show-overflow-tooltip />
                </el-table>
                <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg><p class="rd-empty-text">暂无工作经历信息</p></div>
              </div>
            </section>

            <section class="rd-card">
              <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 15c-1 1-1 2 0 3l3 3c1 1 2 1 3 0l3-3c1-1 1-2 0-3l-3-3c-1-1-2-1-3 0z"/><path d="M9 6L6 9c-1 1-1 2 0 3l3 3"/></svg></span>培训/职称/资格认证</div></div>
              <div class="rd-card-body" style="display:block">
                <el-table :data="viewData.qualificationList || []" border size="small" v-if="viewData.qualificationList && viewData.qualificationList.length">
                  <el-table-column label="序号" type="index" :width="colWidth('序号', 85)" align="center" />
                  <el-table-column label="名称" prop="qualName" align="center" min-width="150" show-overflow-tooltip />
                  <el-table-column label="证书编号" prop="qualNo" align="center" width="120" />
                  <el-table-column label="发证机构" prop="issueOrg" align="center" min-width="120" show-overflow-tooltip />
                  <el-table-column label="发证日期" prop="issueDate" align="center" width="120" />
                  <el-table-column label="有效期至" prop="validTo" align="center" width="120" />
                  <el-table-column label="成绩/等级" prop="score" align="center" width="100" />
                </el-table>
                <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg><p class="rd-empty-text">暂无培训/职称/资格认证信息</p></div>
              </div>
            </section>

            <section class="rd-card">
              <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg></span>系统信息</div></div>
              <div class="rd-card-body" style="display:block">
                <template v-if="viewData.userId">
                  <div class="rd-grid">
                    <div class="rd-item"><span class="rd-label">登录账号</span><div class="rd-value">{{ viewData.userName || '-' }}</div></div>
                    <div class="rd-item"><span class="rd-label">用户昵称</span><div class="rd-value">{{ viewData.userNickName || '-' }}</div></div>
                    <div class="rd-item"><span class="rd-label">账号状态</span><div class="rd-value"><span class="badge green" v-if="viewData.userStatus === '0'">正常</span><span class="badge red" v-else-if="viewData.userStatus === '1'">停用</span><span v-else>-</span></div></div>
                    <div class="rd-item"><span class="rd-label">系统角色</span><div class="rd-value">{{ viewData.userRoles || '-' }}</div></div>
                    <div class="rd-item"><span class="rd-label">最近登录IP</span><div class="rd-value">{{ viewData.userLoginIp || '-' }}</div></div>
                    <div class="rd-item"><span class="rd-label">最近登录时间</span><div class="rd-value">{{ viewData.userLoginDate || '-' }}</div></div>
                    <div class="rd-item"><span class="rd-label">账号创建时间</span><div class="rd-value">{{ viewData.userCreateTime || '-' }}</div></div>
                  </div>
                  <el-alert type="info" :closable="false" show-icon style="margin-top: 12px" title="账号信息请前往 系统管理-用户管理 维护" />
                </template>
                <div class="rd-empty" v-else><svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg><p class="rd-empty-text">未关联系统账号</p></div>
              </div>
            </section>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- ===== 导入对话框 ===== -->
    <excel-import-dialog
      ref="importRef"
      title="员工档案导入"
      action="/hr/employee/importData"
      template-action="/hr/employee/importTemplate"
      template-file-name="hr_employee_template"
      update-support-label="是否更新已经存在的员工数据"
      :tips="importTips"
      :update-key-options="updateKeyOptions"
      default-update-key="employeeNo"
      @success="getList"
    />

    <!-- ===== 部门选择弹窗 ===== -->
    <dept-picker ref="orgPickerRef" title="选择所属部门" :disabled-ids="[100]" @confirm="onOrgPickerConfirm" />

    <!-- ===== 岗位选择弹窗 ===== -->
    <position-picker ref="positionPickerRef" @confirm="onPositionPickerConfirm" />

    <!-- ===== 角色选择弹窗 ===== -->
    <role-picker ref="rolePickerRef" title="选择角色" @confirm="onRolePickerConfirm" />

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { getCurrentInstance } from 'vue';
import { listHrEmployee, getHrEmployee, delHrEmployee, addHrEmployee, updateHrEmployee } from '@/api/hr/hrEmployee';
import { Filter, ArrowDown, Search, Edit, Delete, Download, Upload } from '@element-plus/icons-vue';
import ImageUpload from '@/components/ImageUpload';
import DeptPicker from '@/components/DeptPicker';
import PositionPicker from '@/components/PositionPicker';
import RolePicker from '@/components/RolePicker';
import ExcelImportDialog from '@/components/ExcelImportDialog';
import { listRole } from '@/api/system/role';
import { deptTreeSelect } from '@/api/system/user';
import { useColumnResize } from '@/composables/useColumnResize';

const { proxy } = getCurrentInstance();

const { hr_employee_status, hr_gender, hr_marital_status, hr_edu_level, hr_employee_type, hr_work_type, hr_social_security_status, sys_normal_disable } = proxy.useDict('hr_employee_status', 'hr_gender', 'hr_marital_status', 'hr_edu_level', 'hr_employee_type', 'hr_work_type', 'hr_social_security_status', 'sys_normal_disable');

const hrEmployeeList = ref([]);
const open = ref(false);
const viewOpen = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref('');
const activeTab = ref('basic');
const collapsedCards = reactive({ c0: false, c1: false, c2: false, c3: false, s0: false, d0: false, d1: false, d2: false, d3: false });
const roleOptions = ref([]);
listRole({ pageNum: 1, pageSize: 100, status: '0' }).then(res => { roleOptions.value = res.rows || []; });
const deptOptions = ref([]);
deptTreeSelect().then(res => { deptOptions.value = res.data; });
const showFullInfo = ref(false);
const showAdvanced = ref(false);
const dateRange = ref([]);
const orgPickerRef = ref(null);
const positionPickerRef = ref(null);
const rolePickerRef = ref(null);
const importRef = ref(null);

/** 列显隐与列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_employee_index');

const defaultColumns = {
  employeeNo: { label: '工号', visible: true },
  employeeName: { label: '姓名', visible: true },
  gender: { label: '性别', visible: true },
  positionName: { label: '岗位', visible: true },
  idCard: { label: '身份证号', visible: true },
  phone: { label: '联系电话', visible: true },
  entryDate: { label: '入职日期', visible: true },
  employeeStatus: { label: '状态', visible: true },
};
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_employee_columns');
    if (saved) {
      const parsed = JSON.parse(saved);
      const result = {};
      Object.keys(defaultColumns).forEach(key => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible };
      });
      return result;
    }
  } catch (e) {}
  return { ...defaultColumns };
}
const columns = ref(loadColumnVisibility());

/** 状态页签 */
const activeStatusTab = ref('all');
const statusCounts = ref({ all: 0 });

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  employeeNo: undefined,
  employeeName: undefined,
  idCard: undefined,
  employeeStatus: undefined,
  gender: undefined,
  positionName: undefined,
  phone: undefined,
  email: undefined,
  eduLevel: undefined,
  employeeType: undefined,
  orgId: undefined,
});

const activeFilterCount = computed(() => {
  let count = 0;
  if (queryParams.employeeNo) count++;
  if (queryParams.employeeName) count++;
  if (queryParams.idCard) count++;
  if (queryParams.employeeStatus) count++;
  if (queryParams.gender) count++;
  if (queryParams.positionName) count++;
  if (queryParams.phone) count++;
  if (queryParams.email) count++;
  if (queryParams.eduLevel) count++;
  if (queryParams.employeeType) count++;
  if (queryParams.orgId) count++;
  if (dateRange.value && dateRange.value.length === 2) count++;
  return count;
});

const form = ref({
  employeeId: null,
  employeeNo: '',
  employeeName: '',
  gender: '',
  idCard: '',
  birthday: null,
  nation: '',
  politicsStatus: '',
  maritalStatus: '',
  phone: '',
  email: '',
  emergencyContact: '',
  emergencyPhone: '',
  nativePlace: '',
  address: '',
  eduLevel: '',
  graduationSchool: '',
  major: '',
  graduationDate: null,
  socialWorkYears: null,
  companyWorkYears: null,
  orgId: null,
  orgName: '',
  positionId: null,
  positionName: '',
  skillLevel: '',
  workType: '',
  employeeType: '',
  entryDate: null,
  regularDate: null,
  leaveDate: null,
  employeeStatus: '0',
  bankName: '',
  bankAccount: '',
  socialSecurityNo: '',
  socialSecurityStatus: '',
  houseFundNo: '',
  leaveReason: '',
  userId: null,
  userNickName: '',
  userName: '',
  password: '',
  confirmPassword: '',
  roleIds: [],
  photo: '',
  status: '0',
  delFlag: '0',
  remark: '',
  familyList: [],
  educationList: [],
  workExpList: [],
  qualificationList: [],
});

const viewData = ref({});

const rules = {
    employeeName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  idCard: [{ required: true, message: '身份证号不能为空', trigger: 'blur' }, { validator: validateIdCard, trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  orgId: [{ required: true, message: '所属部门不能为空', trigger: 'change' }],
  positionName: [{ required: true, message: '岗位不能为空', trigger: 'change' }],
};

/** ===== 身份证号校验与自动识别 ===== */
const idCardRegex = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9X]$/;
function isValidIdCard(v) {
  if (!idCardRegex.test(v)) return false;
  const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
  const codes = '10X98765432';
  let sum = 0;
  for (let i = 0; i < 17; i++) sum += Number(v[i]) * weights[i];
  return codes[sum % 11] === v[17];
}
function validateIdCard(rule, value, callback) {
  if (!value) return callback();
  const v = String(value).toUpperCase();
  if (!idCardRegex.test(v)) return callback(new Error('身份证号格式不正确，应为18位'));
  if (!isValidIdCard(v)) return callback(new Error('身份证校验位不正确，请核对'));
  callback();
}
/** 身份证号录入满18位且合法时，自动识别性别与出生日期 */
function onIdCardInput(val) {
  const v = String(val || '').toUpperCase();
  if (v.length === 18 && isValidIdCard(v)) {
    form.value.birthday = v.slice(6, 10) + '-' + v.slice(10, 12) + '-' + v.slice(12, 14);
    form.value.gender = Number(v[16]) % 2 === 1 ? '男' : '女';
  }
}

/** ===== 部门/岗位/账号弹框选择 ===== */
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
function openRolePicker() { rolePickerRef.value.open(form.value.roleIds || []); }
function onRolePickerConfirm(rows) { form.value.roleIds = rows.map(r => r.roleId); }
function clearRoles() { form.value.roleIds = []; }
function onRoleFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.roleIds && form.value.roleIds.length) { clearRoles(); } else { openRolePicker(); }
    return;
  }
  openRolePicker();
}
const roleNames = computed(() => (form.value.roleIds || []).map(id => {
  const r = roleOptions.value.find(x => x.roleId === id);
  return r ? r.roleName : ('角色' + id);
}).join('、'));
function onPositionFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.positionId) { clearPosition(); } else { openPositionPicker(); }
    return;
  }
  openPositionPicker();
}

/** ===== 明细表行操作 ===== */
function addFamilyRow() { form.value.familyList.push({ memberName: '', relationship: '', gender: '', birthday: null, phone: '', occupation: '' }); }
function addEducationRow() { form.value.educationList.push({ startDate: null, endDate: null, schoolName: '', major: '', eduLevel: '', degree: '', isHighest: '0' }); }
function addWorkExpRow() { form.value.workExpList.push({ startDate: null, endDate: null, companyName: '', position: '', workContent: '', leaveReason: '' }); }
function addQualificationRow() { form.value.qualificationList.push({ qualName: '', qualNo: '', issueOrg: '', issueDate: null, validFrom: null, validTo: null, score: '' }); }

/** ===== 导入功能 ===== */
const importTips = [
  '工号、姓名为必填字段，不能为空',
  '性别填：男/女；员工状态填：在职/试用/离职/退休（或留空）',
  '所属组织ID、岗位ID请填写系统中已存在的数字ID',
  '身份证号请填写18位有效号码',
  '如勾选「更新已存在数据」，可选择匹配字段：按工号或身份证号匹配',
  '导入结果将显示每条数据的处理情况，失败数据可修正后重新导入'
];
const updateKeyOptions = [
  { value: 'employeeNo', label: '工号' },
  { value: 'idCard', label: '身份证号' }
];
function handleImport() {
  proxy.$refs['importRef'].open();
}

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key];
}

function handleQuery() {
queryParams.pageNum = 1;
queryParams.params = proxy.addDateRange(queryParams.params, dateRange.value, 'EntryDate');
getList();
loadStatusCounts();
}

function resetQuery() {
  queryParams.employeeNo = undefined;
  queryParams.employeeName = undefined;
  queryParams.idCard = undefined;
  queryParams.employeeStatus = undefined;
  queryParams.gender = undefined;
  queryParams.positionName = undefined;
  queryParams.phone = undefined;
  queryParams.email = undefined;
  queryParams.eduLevel = undefined;
  queryParams.employeeType = undefined;
queryParams.orgId = undefined;
dateRange.value = [];
activeStatusTab.value = 'all';
queryParams.params = {};
handleQuery();
}

/** 状态页签：统计各状态人数（不受当前筛选影响） */
function loadStatusCounts() {
  listHrEmployee({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: 0 };
    (res.rows || []).forEach(row => {
      const s = row.employeeStatus || '-';
      counts[s] = (counts[s] || 0) + 1;
      counts.all++;
    });
    statusCounts.value = counts;
  }).catch(() => {});
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status;
  queryParams.employeeStatus = status === 'all' ? undefined : status;
  handleQuery();
}

function statusTabClass(status) {
  const map = { '0': 'tab-working', '1': 'tab-probation', '2': 'tab-quit', '4': 'tab-retired' };
  return map[status] || '';
}

function statusLabel(v) {
if (v === undefined || v === null || v === '') return '-';
const d = (hr_employee_status.value || []).find(i => i.value === String(v));
return d ? d.label : String(v);
}

/** 通用字典标签转换（未匹配时原样显示，兼容历史中文数据；入参兼容 ref 与数组） */
function dictLabel(dictList, v) {
if (v === undefined || v === null || v === '') return '-';
const list = Array.isArray(dictList) ? dictList : (dictList && dictList.value) || [];
const d = list.find(i => i.value === String(v));
return d ? d.label : String(v);
}

function getList() {
  loading.value = true;
  listHrEmployee(queryParams).then(res => {
    hrEmployeeList.value = res.rows;
    total.value = res.total;
  }).catch(() => {
    loading.value = false;
  }).finally(() => {
    loading.value = false;
  });
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.employeeId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function reset() {
  form.value = {
    employeeId: null,
    employeeNo: '',
    employeeName: '',
    gender: '',
    idCard: '',
    birthday: null,
    nation: '',
    politicsStatus: '',
    maritalStatus: '',
    phone: '',
    email: '',
    emergencyContact: '',
    emergencyPhone: '',
    nativePlace: '',
    address: '',
    eduLevel: '',
    graduationSchool: '',
    major: '',
    graduationDate: null,
    socialWorkYears: null,
    companyWorkYears: null,
    orgId: null,
    orgName: '',
    positionId: null,
    positionName: '',
    skillLevel: '',
    workType: '',
    employeeType: '',
    entryDate: null,
    regularDate: null,
    leaveDate: null,
    employeeStatus: '0',
    bankName: '',
    bankAccount: '',
    socialSecurityNo: '',
    socialSecurityStatus: '',
    houseFundNo: '',
    leaveReason: '',
    userId: null,
    userNickName: '',
    userName: '',
    password: '',
    confirmPassword: '',
    roleIds: [],
    photo: '',
    status: '0',
    delFlag: '0',
    remark: '',
    familyList: [],
    educationList: [],
    workExpList: [],
    qualificationList: [],
  };
  showFullInfo.value = false;
  proxy.resetForm('hrEmployeeRef');
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = '新增员工档案';
}

function handleUpdate(row) {
  reset();
  const id = row.employeeId || ids.value[0];
  getHrEmployee(id).then(res => {
    form.value = res.data;
    form.value.familyList = res.data.familyList || [];
    form.value.educationList = res.data.educationList || [];
    form.value.workExpList = res.data.workExpList || [];
    form.value.qualificationList = res.data.qualificationList || [];
    open.value = true;
    title.value = '修改员工档案';
  });
}

function handleView(row) {
  const id = row.employeeId;
  getHrEmployee(id).then(res => {
    viewData.value = res.data;
    activeTab.value = 'basic';
    viewOpen.value = true;
  });
}

function submitForm() {
  proxy.$refs['hrEmployeeRef'].validate(valid => {
    if (valid) {
      // 系统信息联动校验：填写了账号时必须设置密码且两次输入一致
      if (!form.value.userId && form.value.userName) {
        if (!form.value.password) { proxy.$modal.msgError('填写了登录账号，请设置初始密码'); return }
        if (form.value.password !== form.value.confirmPassword) { proxy.$modal.msgError('两次输入的密码不一致'); return }
      }
      // 已关联账号的员工不再提交账号创建字段
      if (form.value.userId) {
        form.value.userName = undefined;
        form.value.password = undefined;
        form.value.confirmPassword = undefined;
        form.value.roleIds = [];
      }
      if (form.value.employeeId != null) {
        updateHrEmployee(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功');
          open.value = false;
          getList();
        });
      } else {
        addHrEmployee(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功');
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const deleteIds = row.employeeId ? [row.employeeId] : ids.value;
  proxy.$modal.confirm('是否确认删除选中的员工数据？').then(() => {
    return delHrEmployee(deleteIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess('删除成功');
  }).catch(() => {});
}

function handleExport() {
  proxy.download('hr/employee/export', { ...queryParams }, 'employee_' + new Date().getTime() + '.xlsx');
}

function cancel() {
  open.value = false;
  reset();
}

onMounted(() => {
getList();
loadStatusCounts();
});
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-employee-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.hr-employee-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Status Tabs ===== */
.hr-employee-page .status-tabs { padding:12px 20px 0; }
.hr-employee-page .tabs-track { display:flex; align-items:center; gap:4px; flex-wrap:wrap; }
.hr-employee-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.hr-employee-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.hr-employee-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.hr-employee-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.hr-employee-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.hr-employee-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.hr-employee-page .status-tab.is-active .dot { background:var(--brand-500); }
/* 在职 - green */
.hr-employee-page .status-tab.tab-working .dot { background:var(--green-500); }
.hr-employee-page .status-tab.tab-working .count { background:var(--green-50); color:var(--green-700); }
.hr-employee-page .status-tab.is-active.tab-working .count { background:var(--green-500); color:#fff; }
/* 试用 - blue */
.hr-employee-page .status-tab.tab-probation .dot { background:var(--blue-500); }
.hr-employee-page .status-tab.tab-probation .count { background:var(--blue-50); color:var(--blue-700); }
.hr-employee-page .status-tab.is-active.tab-probation .count { background:var(--blue-500); color:#fff; }
/* 离职 - red */
.hr-employee-page .status-tab.tab-quit .dot { background:var(--red-500); }
.hr-employee-page .status-tab.tab-quit .count { background:var(--red-50); color:var(--red-700); }
.hr-employee-page .status-tab.is-active.tab-quit .count { background:var(--red-500); color:#fff; }
/* 退休 - gray */
.hr-employee-page .status-tab.tab-retired .dot { background:var(--ink-400); }
.hr-employee-page .status-tab.tab-retired .count { background:var(--ink-100); color:var(--ink-500); }
.hr-employee-page .status-tab.is-active.tab-retired .count { background:var(--ink-400); color:#fff; }

/* ===== Filter Card ===== */
.hr-employee-page .filter-card { padding:14px 20px 16px; }
.hr-employee-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-employee-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-employee-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-employee-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-employee-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-employee-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-employee-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-employee-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-employee-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-employee-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-employee-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-employee-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-employee-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-employee-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-employee-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
/* el-input transparent inside .control */
.hr-employee-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-employee-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-employee-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-employee-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-employee-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
/* el-select / tree-select transparent inside .control */
.hr-employee-page .field .control :deep(.el-select) { width:100%; }
.hr-employee-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-employee-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-employee-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-employee-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-employee-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-employee-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-employee-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-employee-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-employee-page .btn-soft .el-icon { font-size:14px; }
.hr-employee-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-employee-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-employee-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-employee-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-employee-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-employee-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-employee-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-employee-page .table-wrap { overflow-x:auto; }
.hr-employee-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-employee-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-employee-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-employee-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-employee-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-employee-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-employee-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-employee-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-employee-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-employee-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-employee-page .badge { display:inline-flex; align-items:center; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-employee-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-employee-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-employee-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }

/* ===== Pagination ===== */
.hr-employee-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-employee-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-employee-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-employee-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-employee-page .pagination-container :deep(.el-pagination .btn-prev), .hr-employee-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-employee-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-employee-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-employee-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
.hr-employee-page .full-toggle-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 16px;
  margin-bottom: 12px;
  border-radius: 8px;
  background: #f0f7ff;
  border: 1px solid #d6e8ff;
}
.hr-employee-page .full-toggle-tip {
  font-size: 13px;
  color: #606266;
}
.hr-employee-page .rd-page {
  max-height: 60vh;
  overflow-y: auto;
}
.hr-employee-page .rd-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
}
.hr-employee-page .rd-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f5f7fa;
  cursor: pointer;
  user-select: none;
}
.hr-employee-page .rd-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.hr-employee-page .rd-card-icon {
  display: flex;
  align-items: center;
  color: #409eff;
}
.hr-employee-page .rd-collapse-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  color: #909399;
  transition: transform 0.2s;
}
.hr-employee-page .rd-collapse-btn.is-collapsed {
  transform: rotate(-90deg);
}
.hr-employee-page .rd-card-body {
  padding: 16px;
}
.hr-employee-page .rd-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 24px;
}
.hr-employee-page .rd-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.hr-employee-page .rd-item--full {
  grid-column: 1 / -1;
}
.hr-employee-page .rd-label {
  font-size: 12px;
  color: #909399;
}
.hr-employee-page .rd-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.hr-employee-page .rd-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  color: #c0c4cc;
}
.hr-employee-page .rd-empty-icon {
  margin-bottom: 8px;
}
.hr-employee-page .rd-empty-text {
  font-size: 13px;
  margin: 0;
}
.hr-employee-page .detail-tabs {
  min-height: 300px;
}
.hr-employee-page .rd-detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.hr-employee-page .rd-detail-header-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
}
.hr-employee-page .rd-detail-header-main {
  flex: 1;
}
.hr-employee-page .rd-detail-header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.hr-employee-page .rd-detail-header-sub {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}
.hr-employee-page .rd-detail-header-divider {
  width: 1px;
  height: 12px;
  background: #dcdfe6;
}
.hr-employee-page .rd-detail-header-no {
  font-size: 13px;
  color: #909399;
}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-employee-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-employee-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-employee-page .toolbar { flex-wrap:wrap; gap:10px; } .hr-employee-page .status-tabs { padding:8px 12px 0; } }
</style>
