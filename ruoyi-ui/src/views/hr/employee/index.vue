<template>
  <div class="app-container hr-employee-page">
    <!-- 筛选条件卡片 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>工号</label>
          <div class="control"><el-input v-model="queryParams.employeeNo" placeholder="请输入工号" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>姓名</label>
          <div class="control"><el-input v-model="queryParams.employeeName" placeholder="请输入姓名" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>身份证号</label>
          <div class="control"><el-input v-model="queryParams.idCard" placeholder="请输入身份证号" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>员工状态</label>
          <div class="control">
            <el-select v-model="queryParams.employeeStatus" placeholder="请选择" clearable style="width: 100%">
              <el-option label="在职" value="在职" /><el-option label="试用" value="试用" /><el-option label="离职" value="离职" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件</div>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:employee:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:employee:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:employee:remove']">删除</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:employee:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="hrEmployeeList" @selection-change="handleSelectionChange" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" width="85" align="center" />
          <el-table-column label="工号" prop="employeeNo" align="center" width="120" />
          <el-table-column label="姓名" prop="employeeName" align="center" width="100" />
          <el-table-column label="性别" prop="gender" align="center" width="80" />
          <el-table-column label="岗位" prop="positionName" align="center" min-width="120" show-overflow-tooltip />
          <el-table-column label="身份证号" prop="idCard" align="center" width="180" show-overflow-tooltip />
          <el-table-column label="联系电话" prop="phone" align="center" width="120" />
          <el-table-column label="入职日期" prop="entryDate" align="center" width="120" />
          <el-table-column label="状态" prop="employeeStatus" align="center" width="100">
            <template #default="scope">
              <span class="badge green" v-if="scope.row.employeeStatus === '在职'">{{ scope.row.employeeStatus }}</span>
              <span class="badge red" v-else-if="scope.row.employeeStatus === '离职'">{{ scope.row.employeeStatus }}</span>
              <span class="badge amber" v-else>{{ scope.row.employeeStatus || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right" class-name="col-action">
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
    <el-dialog v-model="open" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrEmployeeRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="工号" prop="employeeNo"><el-input v-model="form.employeeNo" placeholder="请输入工号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="姓名" prop="employeeName"><el-input v-model="form.employeeName" placeholder="请输入姓名" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="性别" prop="gender"><el-select v-model="form.gender" placeholder="请选择" style="width: 100%"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="身份证号" prop="idCard"><el-input v-model="form.idCard" placeholder="请输入身份证号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="出生日期" prop="birthday"><el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="民族" prop="nation"><el-input v-model="form.nation" placeholder="请输入民族" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="联系电话" prop="phone"><el-input v-model="form.phone" placeholder="请输入联系电话" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="邮箱" prop="email"><el-input v-model="form.email" placeholder="请输入邮箱" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="所属组织ID" prop="orgId"><el-input-number v-model="form.orgId" :precision="0" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="岗位名称" prop="positionName"><el-input v-model="form.positionName" placeholder="请输入岗位名称" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="照片" prop="photo"><el-input v-model="form.photo" placeholder="请输入照片URL" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="员工状态" prop="employeeStatus"><el-select v-model="form.employeeStatus" placeholder="请选择" style="width: 100%"><el-option label="在职" value="在职" /><el-option label="试用" value="试用" /><el-option label="离职" value="离职" /><el-option label="退休" value="退休" /></el-select></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>个人信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="政治面貌" prop="politicsStatus"><el-select v-model="form.politicsStatus" placeholder="请选择" style="width: 100%"><el-option label="群众" value="群众" /><el-option label="党员" value="党员" /><el-option label="团员" value="团员" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="婚姻状况" prop="maritalStatus"><el-select v-model="form.maritalStatus" placeholder="请选择" style="width: 100%"><el-option label="未婚" value="未婚" /><el-option label="已婚" value="已婚" /><el-option label="离异" value="离异" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="籍贯" prop="nativePlace"><el-input v-model="form.nativePlace" placeholder="请输入籍贯" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="现住址" prop="address"><el-input v-model="form.address" placeholder="请输入现住址" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="紧急联系人" prop="emergencyContact"><el-input v-model="form.emergencyContact" placeholder="请输入紧急联系人" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="紧急电话" prop="emergencyPhone"><el-input v-model="form.emergencyPhone" placeholder="请输入紧急电话" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="学历" prop="eduLevel"><el-input v-model="form.eduLevel" placeholder="请输入学历" /></el-form-item></el-col>
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
                <el-col :span="12"><el-form-item label="用工类型" prop="employeeType"><el-select v-model="form.employeeType" placeholder="请选择" style="width: 100%"><el-option label="正式工" value="正式工" /><el-option label="合同工" value="合同工" /><el-option label="临时工" value="临时工" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工时制度" prop="workType"><el-select v-model="form.workType" placeholder="请选择" style="width: 100%"><el-option label="标准工时" value="标准工时" /><el-option label="综合工时" value="综合工时" /><el-option label="不定时" value="不定时" /></el-select></el-form-item></el-col>
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
                <el-col :span="12"><el-form-item label="黑名单" prop="blacklistFlag"><el-select v-model="form.blacklistFlag" placeholder="请选择" style="width: 100%"><el-option label="否" value="0" /><el-option label="是" value="1" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="黑名单原因" prop="blacklistReason"><el-input v-model="form.blacklistReason" placeholder="请输入黑名单原因" /></el-form-item></el-col>
                <el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item></el-col>
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

    <!-- ===== 查看详情弹窗（三页签卡片式布局） ===== -->
    <el-dialog v-model="viewOpen" width="960px" append-to-body draggable class="rd-dialog">
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
                  <div class="rd-item"><span class="rd-label">性别</span><div class="rd-value">{{ viewData.gender || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">出生日期</span><div class="rd-value">{{ viewData.birthday || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">民族</span><div class="rd-value">{{ viewData.nation || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">联系电话</span><div class="rd-value">{{ viewData.phone || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">邮箱</span><div class="rd-value">{{ viewData.email || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">岗位</span><div class="rd-value">{{ viewData.positionName || '-' }}</div></div>
                </div>
              </div>
            </section>
            <section class="rd-card">
              <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 11V7a4 4 0 0 0-8 0V11"/><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/></svg></span>联系信息</div></div>
              <div class="rd-card-body" style="display:block">
                <div class="rd-grid">
                  <div class="rd-item"><span class="rd-label">紧急联系人</span><div class="rd-value">{{ viewData.emergencyContact || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">紧急电话</span><div class="rd-value">{{ viewData.emergencyPhone || '-' }}</div></div>
                  <div class="rd-item rd-item--full"><span class="rd-label">现住址</span><div class="rd-value">{{ viewData.address || '-' }}</div></div>
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
                  <div class="rd-item"><span class="rd-label">性别</span><div class="rd-value">{{ viewData.gender || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">民族</span><div class="rd-value">{{ viewData.nation || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">政治面貌</span><div class="rd-value">{{ viewData.politicsStatus || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">婚姻状况</span><div class="rd-value">{{ viewData.maritalStatus || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">籍贯</span><div class="rd-value">{{ viewData.nativePlace || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">学历</span><div class="rd-value">{{ viewData.eduLevel || '-' }}</div></div>
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
                  <el-table-column label="序号" type="index" width="85" align="center" />
                  <el-table-column label="姓名" prop="memberName" align="center" min-width="100" />
                  <el-table-column label="关系" prop="relationship" align="center" width="100" />
                  <el-table-column label="性别" prop="gender" align="center" width="80" />
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
                  <div class="rd-item"><span class="rd-label">员工状态</span><div class="rd-value">{{ viewData.employeeStatus || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">用工类型</span><div class="rd-value">{{ viewData.employeeType || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">工时制度</span><div class="rd-value">{{ viewData.workType || '-' }}</div></div>
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
                  <div class="rd-item"><span class="rd-label">参保状态</span><div class="rd-value">{{ viewData.socialSecurityStatus || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">公积金号</span><div class="rd-value">{{ viewData.houseFundNo || '-' }}</div></div>
                  <div class="rd-item"><span class="rd-label">黑名单</span><div class="rd-value">{{ viewData.blacklistFlag === '1' ? '是' : '否' }}</div></div>
                </div>
              </div>
            </section>

            <section class="rd-card">
              <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg></span>教育背景</div></div>
              <div class="rd-card-body" style="display:block">
                <el-table :data="viewData.educationList || []" border size="small" v-if="viewData.educationList && viewData.educationList.length">
                  <el-table-column label="序号" type="index" width="85" align="center" />
                  <el-table-column label="开始日期" prop="startDate" align="center" width="120" />
                  <el-table-column label="结束日期" prop="endDate" align="center" width="120" />
                  <el-table-column label="学校名称" prop="schoolName" align="center" min-width="150" show-overflow-tooltip />
                  <el-table-column label="专业" prop="major" align="center" min-width="100" show-overflow-tooltip />
                  <el-table-column label="学历" prop="eduLevel" align="center" width="100" />
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
                  <el-table-column label="序号" type="index" width="85" align="center" />
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
                  <el-table-column label="序号" type="index" width="85" align="center" />
                  <el-table-column label="类型" prop="qualType" align="center" width="120" />
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
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { getCurrentInstance } from 'vue';
import { listHrEmployee, getHrEmployee, delHrEmployee, addHrEmployee, updateHrEmployee } from '@/api/hr/hrEmployee';
import { Filter } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();

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
const collapsedCards = reactive({ c0: false, c1: false, c2: false, c3: false });

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  employeeNo: undefined,
  employeeName: undefined,
  idCard: undefined,
  employeeStatus: undefined,
});

const activeFilterCount = computed(() => {
  let count = 0;
  if (queryParams.employeeNo) count++;
  if (queryParams.employeeName) count++;
  if (queryParams.idCard) count++;
  if (queryParams.employeeStatus) count++;
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
  positionId: null,
  positionName: '',
  skillLevel: '',
  workType: '',
  employeeType: '',
  entryDate: null,
  regularDate: null,
  leaveDate: null,
  employeeStatus: '',
  bankName: '',
  bankAccount: '',
  socialSecurityNo: '',
  socialSecurityStatus: '',
  houseFundNo: '',
  leaveReason: '',
  blacklistFlag: '0',
  blacklistReason: '',
  userId: null,
  photo: '',
  status: '0',
  delFlag: '0',
  remark: '',
});

const viewData = ref({});

const rules = {
  employeeNo: [{ required: true, message: '工号不能为空', trigger: 'blur' }],
  employeeName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
};

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key];
}

function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm('queryForm');
  handleQuery();
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
    positionId: null,
    positionName: '',
    skillLevel: '',
    workType: '',
    employeeType: '',
    entryDate: null,
    regularDate: null,
    leaveDate: null,
    employeeStatus: '',
    bankName: '',
    bankAccount: '',
    socialSecurityNo: '',
    socialSecurityStatus: '',
    houseFundNo: '',
    leaveReason: '',
    blacklistFlag: '0',
    blacklistReason: '',
    userId: null,
    photo: '',
    status: '0',
    delFlag: '0',
    remark: '',
  };
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
});
</script>

<style scoped>
.hr-employee-page .filter-card {
  margin-bottom: 16px;
  padding: 16px 20px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.hr-employee-page .filter-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.hr-employee-page .filter-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}
.hr-employee-page .filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.hr-employee-page .filter-bar .field {
  flex: 1 1 200px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.hr-employee-page .filter-bar .field label {
  font-size: 12px;
  color: #909399;
}
.hr-employee-page .filter-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}
.hr-employee-page .filter-info {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}
.hr-employee-page .surface {
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
  padding: 16px 20px;
}
.hr-employee-page .toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.hr-employee-page .toolbar .left {
  display: flex;
  gap: 8px;
}
.hr-employee-page .table-wrap {
  margin-bottom: 12px;
}
.hr-employee-page .badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}
.hr-employee-page .badge.green {
  background: #e8f5e9;
  color: #2e7d32;
}
.hr-employee-page .badge.red {
  background: #ffebee;
  color: #c62828;
}
.hr-employee-page .badge.amber {
  background: #fff8e1;
  color: #f57f17;
}
.hr-employee-page .action-btn-row {
  display: flex;
  justify-content: center;
  gap: 4px;
}
.hr-employee-page .pagination-container {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
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
</style>
